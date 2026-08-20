package recruitment.dev.interviewservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.dto.ApplicationResponse;
import recruitment.dev.interviewservice.dto.EmployeeResponse;
import recruitment.dev.interviewservice.dto.WorkflowCompleteTaskRequest;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.entities.InterviewStage;
import recruitment.dev.interviewservice.entities.InterviewType;
import recruitment.dev.interviewservice.exception.BusinessException;
import recruitment.dev.interviewservice.exception.ConflictException;
import recruitment.dev.interviewservice.exception.ResourceNotFoundException;
import recruitment.dev.interviewservice.feign.ApplicationClient;
import recruitment.dev.interviewservice.feign.BearerTokenProvider;
import recruitment.dev.interviewservice.feign.EmployeeClient;
import recruitment.dev.interviewservice.feign.NotificationClient;
import recruitment.dev.interviewservice.feign.WorkflowClient;
import recruitment.dev.interviewservice.mapper.InterviewMapper;
import recruitment.dev.interviewservice.repository.InterviewRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceImplTest {

    @Mock private InterviewRepository repository;
    @Mock private InterviewMapper mapper;
    @Mock private ApplicationClient applicationClient;
    @Mock private EmployeeClient employeeClient;
    @Mock private NotificationClient notificationClient;
    @Mock private WorkflowClient workflowClient;
    @Mock private BearerTokenProvider bearerTokenProvider;
    @InjectMocks private InterviewServiceImpl service;

    private void stubValidReferences() {
        when(bearerTokenProvider.currentAuthorizationHeader()).thenReturn("Bearer test-token");
        when(applicationClient.getApplicationById(anyLong(), anyString())).thenAnswer(invocation -> {
            ApplicationResponse response = new ApplicationResponse();
            response.setId(invocation.getArgument(0));
            response.setCurrentTaskId("task-1");
            response.setCurrentTaskDefinitionKey("hrInterview");
            response.setCurrentTaskName("HR Interview");
            return response;
        });
        when(employeeClient.getEmployeeById(anyLong(), anyString())).thenAnswer(invocation -> {
            EmployeeResponse response = new EmployeeResponse();
            response.setId(invocation.getArgument(0));
            response.setKeycloakId("employee-keycloak-id");
            response.setEmail("employee@example.com");
            return response;
        });
    }

    @Test
    void createsValidatedFutureInterview() {
        stubValidReferences();
        when(repository.findByInterviewerIdAndStatusIn(anyLong(), any())).thenReturn(List.of());
        InterviewDto dto = futureInterview();
        Interview entity = new Interview();
        Interview saved = new Interview();
        entity.setStage(InterviewStage.HR_INTERVIEW);
        InterviewDto expected = InterviewDto.builder().id(4L).build();
        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(expected);

        assertThat(service.create(dto)).isSameAs(expected);
        assertThat(entity.getStage()).isEqualTo(InterviewStage.HR_INTERVIEW);
        verify(repository).save(entity);
        verify(notificationClient).sendNotification(argThat(request ->
                "employee-keycloak-id".equals(request.candidateKeycloakId())
                        && "employee@example.com".equals(request.recipientEmail())
                        && "INTERVIEW_SCHEDULED".equals(request.type())
                        && Long.valueOf(1L).equals(request.applicationId())
        ));
    }

    @Test
    void rejectsPastInterviewBeforePersistence() {
        InterviewDto dto = futureInterview();
        dto.setScheduledAt(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Interview date cannot be in the past");
        verifyNoInteractions(repository, mapper);
    }

    @Test
    void rejectsNonPositiveDurationBeforePersistence() {
        InterviewDto dto = futureInterview();
        dto.setDuration(0);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Duration must be at least 15 minutes");
        verifyNoInteractions(repository, mapper);
    }

    @Test
    void completesInterviewWhenFeedbackIsAdded() {
        when(bearerTokenProvider.currentAuthorizationHeader()).thenReturn("Bearer test-token");
        ApplicationResponse application = new ApplicationResponse();
        application.setId(1L);
        application.setCurrentTaskId("task-1");
        application.setCurrentTaskDefinitionKey("hrInterview");
        when(applicationClient.getApplicationById(1L, "Bearer test-token")).thenReturn(application);
        Interview interview = new Interview();
        interview.setStatus(InterviewStatus.IN_PROGRESS);
        interview.setApplicationId(1L);
        interview.setStage(InterviewStage.HR_INTERVIEW);
        InterviewDto expected = InterviewDto.builder().id(8L).status(InterviewStatus.COMPLETED).build();
        when(repository.findById(8L)).thenReturn(Optional.of(interview));
        when(repository.save(interview)).thenReturn(interview);
        when(mapper.toDto(interview)).thenReturn(expected);

        InterviewDto result = service.addFeedback(8L, "Très bon échange", "RAS", true, InterviewResult.PASSED);

        assertThat(result).isSameAs(expected);
        assertThat(interview.getFeedback()).isEqualTo("Très bon échange");
        assertThat(interview.getNotes()).isEqualTo("RAS");
        assertThat(interview.getResult()).isEqualTo(InterviewResult.PASSED);
        assertThat(interview.getApproved()).isTrue();
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
        verify(workflowClient).completeTask(any(WorkflowCompleteTaskRequest.class), eq("Bearer test-token"));
        verify(repository).save(interview);
    }

    @Test
    void failsWhenInterviewDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void rejectsOverlappingInterviewerSchedule() {
        stubValidReferences();
        Interview scheduled = new Interview();
        scheduled.setId(22L);
        scheduled.setStatus(InterviewStatus.SCHEDULED);
        scheduled.setScheduledAt(LocalDateTime.now().plusDays(1));
        scheduled.setDuration(60);
        when(repository.findByInterviewerIdAndStatusIn(anyLong(), any())).thenReturn(List.of(scheduled));

        InterviewDto request = futureInterview();
        request.setScheduledAt(scheduled.getScheduledAt().plusMinutes(30));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("The interviewer already has an overlapping interview");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsCompletionWithoutFeedback() {
        Interview interview = new Interview();
        interview.setStatus(InterviewStatus.SCHEDULED);
        when(repository.findById(8L)).thenReturn(Optional.of(interview));

        assertThatThrownBy(() -> service.updateStatus(8L, InterviewStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Use the feedback endpoint to complete an interview");
    }

    private InterviewDto futureInterview() {
        return InterviewDto.builder()
                .applicationId(1L).interviewerId(2L)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .duration(45).type(InterviewType.ONLINE).build();
    }
}
