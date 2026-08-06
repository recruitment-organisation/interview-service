package recruitment.dev.interviewservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import recruitment.dev.interviewservice.dto.EmployeeResponse;
import recruitment.dev.interviewservice.feign.BearerTokenProvider;
import recruitment.dev.interviewservice.feign.EmployeeClient;
import recruitment.dev.interviewservice.repository.InterviewRepository;

@Component("interviewAuthorization")
@RequiredArgsConstructor
public class InterviewAuthorization {

    private static final String EMPLOYEE_ROLE = "ROLE_EMPLOYEE";

    private final InterviewRepository interviewRepository;
    private final EmployeeClient employeeClient;
    private final BearerTokenProvider bearerTokenProvider;

    @Transactional(readOnly = true)
    public boolean isAssignedInterviewer(Long interviewId, Authentication authentication) {
        if (!hasEmployeeRole(authentication)) {
            return false;
        }

        return interviewRepository.findById(interviewId)
                .map(interview -> interview.getInterviewerId().equals(currentEmployeeId()))
                .orElse(false);
    }

    public boolean isCurrentInterviewer(Long interviewerId, Authentication authentication) {
        return hasEmployeeRole(authentication) && interviewerId.equals(currentEmployeeId());
    }

    private boolean hasEmployeeRole(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> EMPLOYEE_ROLE.equals(authority.getAuthority()));
    }

    private Long currentEmployeeId() {
        EmployeeResponse employee = employeeClient.getCurrentEmployee(bearerTokenProvider.currentAuthorizationHeader());
        if (employee == null || employee.getId() == null) {
            throw new IllegalStateException("employee-service returned an invalid current employee");
        }
        return employee.getId();
    }
}
