package recruitment.dev.interviewservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import recruitment.dev.interviewservice.dto.EmployeeResponse;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.feign.BearerTokenProvider;
import recruitment.dev.interviewservice.feign.EmployeeClient;
import recruitment.dev.interviewservice.repository.InterviewRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InterviewAuthorizationTest {

    private final InterviewRepository repository = mock(InterviewRepository.class);
    private final EmployeeClient employeeClient = mock(EmployeeClient.class);
    private final BearerTokenProvider bearerTokenProvider = mock(BearerTokenProvider.class);
    private final InterviewAuthorization authorization = new InterviewAuthorization(repository, employeeClient, bearerTokenProvider);

    @Test
    void allowsOnlyTheAssignedEmployee() {
        Interview interview = new Interview();
        interview.setInterviewerId(7L);
        EmployeeResponse employee = new EmployeeResponse();
        employee.setId(7L);
        when(repository.findById(4L)).thenReturn(Optional.of(interview));
        when(bearerTokenProvider.currentAuthorizationHeader()).thenReturn("Bearer employee-token");
        when(employeeClient.getCurrentEmployee("Bearer employee-token")).thenReturn(employee);

        assertThat(authorization.isAssignedInterviewer(4L, employeeAuthentication())).isTrue();
    }

    @Test
    void rejectsUsersWithoutTheEmployeeRoleWithoutCallingDependencies() {
        UsernamePasswordAuthenticationToken candidate = new UsernamePasswordAuthenticationToken(
                "candidate", "n/a", List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
        );

        assertThat(authorization.isAssignedInterviewer(4L, candidate)).isFalse();
        verifyNoInteractions(repository, employeeClient);
    }

    private UsernamePasswordAuthenticationToken employeeAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "employee", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
        );
    }
}
