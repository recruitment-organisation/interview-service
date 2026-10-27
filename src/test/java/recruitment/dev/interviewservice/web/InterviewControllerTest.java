package recruitment.dev.interviewservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.service.InterviewService;
import recruitment.dev.interviewservice.security.InterviewAuthorization;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterviewControllerTest {

    private final InterviewService interviewService = mock(InterviewService.class);
    private final InterviewAuthorization interviewAuthorization = mock(InterviewAuthorization.class);
    private final InterviewController controller = new InterviewController(interviewService, interviewAuthorization);

    @Test
    void employeeCanListOwnInterviewsWithoutCompanyClaim() {
        var pageable = PageRequest.of(0, 100);
        Page<InterviewDto> expected = Page.empty(pageable);
        when(interviewService.findByInterviewerId(10L, pageable)).thenReturn(expected);

        var response = controller.findByInterviewer(10L, pageable, jwtWithRoles("EMPLOYEE"));

        assertThat(response.getBody()).isSameAs(expected);
        verify(interviewService).findByInterviewerId(10L, pageable);
        verify(interviewService, never()).findByInterviewerId(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void managerInterviewerListingRemainsTenantScoped() {
        var pageable = PageRequest.of(0, 20);
        Page<InterviewDto> expected = Page.empty(pageable);
        Jwt jwt = jwtWithRolesAndCompany(25L, "MANAGER");
        when(interviewService.findByInterviewerId(25L, 10L, pageable)).thenReturn(expected);

        var response = controller.findByInterviewer(10L, pageable, jwt);

        assertThat(response.getBody()).isSameAs(expected);
        verify(interviewService).findByInterviewerId(25L, 10L, pageable);
    }

    @Test
    void currentInterviewerEndpointDerivesEmployeeIdFromAuthentication() {
        var pageable = PageRequest.of(0, 100);
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "employee", "n/a", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        Page<InterviewDto> expected = Page.empty(pageable);
        when(interviewAuthorization.currentEmployeeId(authentication)).thenReturn(10L);
        when(interviewService.findByInterviewerId(10L, pageable)).thenReturn(expected);

        var response = controller.findForCurrentInterviewer(pageable, authentication);

        assertThat(response.getBody()).isSameAs(expected);
        verify(interviewService).findByInterviewerId(10L, pageable);
    }

    private Jwt jwtWithRoles(String... roles) {
        return jwtBuilder().claim("realm_access", Map.of("roles", List.of(roles))).build();
    }

    private Jwt jwtWithRolesAndCompany(Long companyId, String... roles) {
        return jwtBuilder()
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .claim("companyId", companyId)
                .build();
    }

    private Jwt.Builder jwtBuilder() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("employee-keycloak-id")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));
    }
}
