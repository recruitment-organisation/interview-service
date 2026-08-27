package recruitment.dev.interviewservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recruitment.dev.interviewservice.dto.ApplicationResponse;
import recruitment.dev.interviewservice.dto.EmployeeResponse;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.dto.SendNotificationRequest;
import recruitment.dev.interviewservice.dto.WorkflowCompleteTaskRequest;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.entities.InterviewStage;
import recruitment.dev.interviewservice.exception.BusinessException;
import recruitment.dev.interviewservice.exception.ConflictException;
import recruitment.dev.interviewservice.exception.DependencyUnavailableException;
import recruitment.dev.interviewservice.exception.ResourceNotFoundException;
import recruitment.dev.interviewservice.feign.ApplicationClient;
import recruitment.dev.interviewservice.feign.BearerTokenProvider;
import recruitment.dev.interviewservice.feign.EmployeeClient;
import recruitment.dev.interviewservice.feign.NotificationClient;
import recruitment.dev.interviewservice.feign.WorkflowClient;
import recruitment.dev.interviewservice.mapper.InterviewMapper;
import recruitment.dev.interviewservice.repository.InterviewRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private static final Set<InterviewStatus> BLOCKING_STATUSES = EnumSet.of(
            InterviewStatus.SCHEDULED,
            InterviewStatus.RESCHEDULED,
            InterviewStatus.IN_PROGRESS
    );
    private static final Map<InterviewStatus, Set<InterviewStatus>> ALLOWED_TRANSITIONS = Map.of(
            InterviewStatus.SCHEDULED, EnumSet.of(InterviewStatus.IN_PROGRESS, InterviewStatus.CANCELLED, InterviewStatus.RESCHEDULED),
            InterviewStatus.RESCHEDULED, EnumSet.of(InterviewStatus.SCHEDULED, InterviewStatus.CANCELLED),
            InterviewStatus.IN_PROGRESS, EnumSet.of(InterviewStatus.CANCELLED)
    );

    private final InterviewRepository repository;
    private final InterviewMapper mapper;
    private final ApplicationClient applicationClient;
    private final EmployeeClient employeeClient;
    private final NotificationClient notificationClient;
    private final WorkflowClient workflowClient;
    private final BearerTokenProvider bearerTokenProvider;

    @Override
    public InterviewDto create(InterviewDto dto) {
        validateInterview(dto);
        ApplicationResponse application = loadApplication(dto.getApplicationId());
        InterviewStage stage = resolveStage(application);
        EmployeeResponse interviewer = loadEmployee(dto.getInterviewerId());
        ensureNoScheduleConflict(dto, null);

        Interview interview = mapper.toEntity(dto);
        interview.setStage(stage);
        interview.setStatus(InterviewStatus.SCHEDULED);
        Interview saved = repository.save(interview);
        notifyAssignedInterviewer(dto, interviewer);
        return mapper.toDto(saved);
    }

    @Override
    public InterviewDto update(Long id, InterviewDto dto) {
        Interview interview = getInterview(id);
        ensureInterviewCanBeEdited(interview);
        validateInterview(dto);
        loadApplication(dto.getApplicationId());
        EmployeeResponse interviewer = loadEmployee(dto.getInterviewerId());
        ensureNoScheduleConflict(dto, id);

        boolean wasRescheduled = !dto.getScheduledAt().equals(interview.getScheduledAt());
        boolean wasReassigned = !dto.getInterviewerId().equals(interview.getInterviewerId());
        mapper.updateEntity(dto, interview);
        if (wasRescheduled) {
            interview.setStatus(InterviewStatus.RESCHEDULED);
        }

        Interview saved = repository.save(interview);
        if (wasRescheduled || wasReassigned) {
            notifyAssignedInterviewer(dto, interviewer);
        }
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewDto findById(Long id) {
        return mapper.toDto(getInterview(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findByApplicationId(Long applicationId, Pageable pageable) {
        return repository.findByApplicationId(applicationId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findByInterviewerId(Long interviewerId, Pageable pageable) {
        return repository.findByInterviewerId(interviewerId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findByStatus(InterviewStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable).map(mapper::toDto);
    }

    @Override
    public void delete(Long id) {
        Interview interview = getInterview(id);
        if (interview.getStatus() == InterviewStatus.IN_PROGRESS || interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ConflictException("An in-progress or completed interview cannot be deleted");
        }
        repository.delete(interview);
    }

    @Override
    public InterviewDto updateStatus(Long id, InterviewStatus status) {
        Interview interview = getInterview(id);
        if (status == null) {
            throw new BusinessException("Interview status is required");
        }
        if (status == interview.getStatus()) {
            return mapper.toDto(interview);
        }
        if (status == InterviewStatus.COMPLETED) {
            throw new BusinessException("Use the feedback endpoint to complete an interview");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(interview.getStatus(), Set.of()).contains(status)) {
            throw new ConflictException("Cannot transition interview from " + interview.getStatus() + " to " + status);
        }

        interview.setStatus(status);
        return mapper.toDto(repository.save(interview));
    }

    @Override
    public InterviewDto addFeedback(Long id, String feedback, String notes, Boolean approved, InterviewResult result) {
        return addFeedback(id, feedback, notes, approved, result, null, null, null);
    }

    @Override
    public InterviewDto addFeedback(
            Long id,
            String feedback,
            String notes,
            Boolean approved,
            InterviewResult result,
            Long departmentId,
            Long employeeRoleId,
            String position
    ) {
        Interview interview = getInterview(id);
        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new ConflictException("Feedback can only be added to an in-progress interview");
        }
        if (feedback == null || feedback.isBlank()) {
            throw new BusinessException("Feedback is required");
        }

        boolean finalApproved = resolveDecision(approved, result);

        interview.setFeedback(feedback.trim());
        interview.setNotes(notes);
        interview.setApproved(finalApproved);
        interview.setResult(finalApproved ? InterviewResult.PASSED : InterviewResult.FAILED);
        interview.setStatus(InterviewStatus.COMPLETED);

        validateManagerHiringData(interview, finalApproved, departmentId, employeeRoleId, position);

        Interview saved = repository.save(interview);
        completeWorkflowTask(saved, finalApproved, departmentId, employeeRoleId, position);
        return mapper.toDto(saved);
    }

    private Interview getInterview(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found: " + id));
    }

    private void validateInterview(InterviewDto dto) {
        if (dto == null) {
            throw new BusinessException("Interview request is required");
        }
        if (dto.getApplicationId() == null) {
            throw new BusinessException("Application is required");
        }
        if (dto.getInterviewerId() == null) {
            throw new BusinessException("Interviewer is required");
        }
        if (dto.getType() == null) {
            throw new BusinessException("Interview type is required");
        }
        if (dto.getDuration() == null || dto.getDuration() < 15) {
            throw new BusinessException("Duration must be at least 15 minutes");
        }
        if (dto.getScheduledAt() == null || !dto.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Interview date cannot be in the past");
        }
    }

    private ApplicationResponse loadApplication(Long applicationId) {
        try {
            ApplicationResponse application = applicationClient.getApplicationById(
                    applicationId, bearerTokenProvider.currentAuthorizationHeader());
            if (application == null || !applicationId.equals(application.getId())) {
                throw new BusinessException("Application not found: " + applicationId);
            }
            return application;
        } catch (FeignException.NotFound exception) {
            throw new BusinessException("Application not found: " + applicationId);
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Unable to validate application-service", exception);
        }
    }

    private EmployeeResponse loadEmployee(Long interviewerId) {
        try {
            EmployeeResponse employee = employeeClient.getEmployeeById(
                    interviewerId, bearerTokenProvider.currentAuthorizationHeader());
            if (employee == null || !interviewerId.equals(employee.getId())) {
                throw new BusinessException("Interviewer not found: " + interviewerId);
            }
            if (employee.getKeycloakId() == null || employee.getKeycloakId().isBlank()) {
                throw new BusinessException("Interviewer has no notification identity: " + interviewerId);
            }
            return employee;
        } catch (FeignException.NotFound exception) {
            throw new BusinessException("Interviewer not found: " + interviewerId);
        } catch (FeignException exception) {
            throw new DependencyUnavailableException("Unable to validate employee-service", exception);
        }
    }

    private void notifyAssignedInterviewer(InterviewDto interview, EmployeeResponse interviewer) {
        String mode = switch (interview.getType()) {
            case ONLINE -> interview.getMeetingLink() == null || interview.getMeetingLink().isBlank()
                    ? "en ligne"
                    : "en ligne : " + interview.getMeetingLink();
            case ONSITE -> interview.getLocation() == null || interview.getLocation().isBlank()
                    ? "en présentiel"
                    : "en présentiel : " + interview.getLocation();
            case PHONE -> "par téléphone";
        };
        String message = "Vous avez été sélectionné pour mener un entretien le "
                + interview.getScheduledAt() + " (" + interview.getDuration() + " min, " + mode + ").";

        notificationClient.sendNotification(new SendNotificationRequest(
                null,
                interviewer.getKeycloakId(),
                interview.getApplicationId(),
                interviewer.getEmail(),
                "INTERVIEW_SCHEDULED",
                "Nouvel entretien assigné",
                message
        ));
    }

    private InterviewStage resolveStage(ApplicationResponse application) {
        return InterviewStage.fromTaskDefinitionKey(application.getCurrentTaskDefinitionKey())
                .orElseThrow(() -> new BusinessException(
                        "Application " + application.getId() + " is not awaiting an interview task"
                ));
    }

    private boolean resolveDecision(Boolean approved, InterviewResult result) {
        if (approved != null) {
            if (result != null && result != InterviewResult.PENDING) {
                boolean resultApproved = result == InterviewResult.PASSED;
                if (resultApproved != approved) {
                    throw new BusinessException("Approved flag and result are inconsistent");
                }
            }
            return approved;
        }

        if (result == null || result == InterviewResult.PENDING) {
            throw new BusinessException("An interview decision requires approved or a final result");
        }

        return result == InterviewResult.PASSED;
    }

    private void completeWorkflowTask(
            Interview interview,
            boolean approved,
            Long departmentId,
            Long employeeRoleId,
            String position
    ) {
        ApplicationResponse application = loadApplication(interview.getApplicationId());
        InterviewStage stage = interview.getStage();
        if (stage == null) {
            stage = resolveStage(application);
        }
        if (!stage.taskDefinitionKey().equals(application.getCurrentTaskDefinitionKey())) {
            throw new BusinessException("Interview stage does not match the current workflow task");
        }
        if (application.getCurrentTaskId() == null || application.getCurrentTaskId().isBlank()) {
            throw new BusinessException("Application current task is missing");
        }

        WorkflowCompleteTaskRequest request = new WorkflowCompleteTaskRequest();
        request.setTaskId(application.getCurrentTaskId());
        request.getVariables().put(stage.workflowVariable(), approved);
        request.getVariables().put(stage.commentVariable(), interview.getFeedback());
        if (stage == InterviewStage.MANAGER_INTERVIEW && approved) {
            request.getVariables().put("departmentId", departmentId);
            request.getVariables().put("employeeRoleId", employeeRoleId);
            request.getVariables().put("position", position.trim());
        }

        workflowClient.completeTask(request, bearerTokenProvider.currentAuthorizationHeader());
    }

    private void validateManagerHiringData(
            Interview interview,
            boolean approved,
            Long departmentId,
            Long employeeRoleId,
            String position
    ) {
        if (interview.getStage() != InterviewStage.MANAGER_INTERVIEW || !approved) {
            return;
        }
        if (departmentId == null || departmentId < 1) {
            throw new BusinessException("Department is required for an accepted manager decision");
        }
        if (employeeRoleId == null || employeeRoleId < 1) {
            throw new BusinessException("Employee role is required for an accepted manager decision");
        }
        if (position == null || position.isBlank()) {
            throw new BusinessException("Position is required for an accepted manager decision");
        }
    }

    private void ensureNoScheduleConflict(InterviewDto dto, Long excludedInterviewId) {
        LocalDateTime requestedStart = dto.getScheduledAt();
        LocalDateTime requestedEnd = requestedStart.plusMinutes(dto.getDuration());

        boolean overlaps = repository.findByInterviewerIdAndStatusIn(dto.getInterviewerId(), BLOCKING_STATUSES)
                .stream()
                .filter(interview -> !interview.getId().equals(excludedInterviewId))
                .anyMatch(interview -> requestedStart.isBefore(interview.getScheduledAt().plusMinutes(interview.getDuration()))
                        && interview.getScheduledAt().isBefore(requestedEnd));
        if (overlaps) {
            throw new ConflictException("The interviewer already has an overlapping interview");
        }
    }

    private void ensureInterviewCanBeEdited(Interview interview) {
        if (interview.getStatus() == InterviewStatus.IN_PROGRESS
                || interview.getStatus() == InterviewStatus.COMPLETED
                || interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ConflictException("This interview can no longer be edited");
        }
    }
}
