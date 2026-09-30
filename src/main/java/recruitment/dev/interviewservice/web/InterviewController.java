package recruitment.dev.interviewservice.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.service.InterviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.AccessDeniedException;


@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
@Validated
public class InterviewController {


    private final InterviewService interviewService;


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PostMapping
    public ResponseEntity<InterviewDto> create(
            @Valid @RequestBody InterviewDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(interviewService.create(dto));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<InterviewDto> update(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InterviewDto dto) {
        ensureTenant(jwt, interviewService.findById(id));
        return ResponseEntity.ok(
                interviewService.update(id, dto)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @interviewAuthorization.isAssignedInterviewer(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<InterviewDto> findById(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        InterviewDto interview = interviewService.findById(id);
        ensureTenant(jwt, interview);
        return ResponseEntity.ok(interview);
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping
    public ResponseEntity<Page<InterviewDto>> findAll(
            Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(isAdmin(jwt) ? interviewService.findAll(pageable) : interviewService.findAll(requiredCompany(jwt), pageable));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<Page<InterviewDto>> findByApplication(
            @PathVariable Long applicationId,
            Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(isAdmin(jwt) ? interviewService.findByApplicationId(applicationId, pageable) : interviewService.findByApplicationId(requiredCompany(jwt), applicationId, pageable));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @interviewAuthorization.isCurrentInterviewer(#interviewerId, authentication)")
    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<Page<InterviewDto>> findByInterviewer(
            @PathVariable Long interviewerId,
            Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(isAdmin(jwt) ? interviewService.findByInterviewerId(interviewerId, pageable) : interviewService.findByInterviewerId(requiredCompany(jwt), interviewerId, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<InterviewDto>> findByStatus(
            @PathVariable InterviewStatus status,
            Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(isAdmin(jwt) ? interviewService.findByStatus(status, pageable) : interviewService.findByStatus(requiredCompany(jwt), status, pageable));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @interviewAuthorization.isAssignedInterviewer(#id, authentication)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<InterviewDto> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam InterviewStatus status) {
        ensureTenant(jwt, interviewService.findById(id));
        return ResponseEntity.ok(
                interviewService.updateStatus(id, status)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER') or @interviewAuthorization.isAssignedInterviewer(#id, authentication)")
    @PatchMapping("/{id}/feedback")
    public ResponseEntity<InterviewDto> addFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @NotBlank String feedback,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) InterviewResult result,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeRoleId,
            @RequestParam(required = false) String position) {

        ensureTenant(jwt, interviewService.findById(id));
        return ResponseEntity.ok(
                interviewService.addFeedback(
                        id,
                        feedback,
                        notes,
                        approved,
                        result,
                        departmentId,
                        employeeRoleId,
                        position
                )
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ensureTenant(jwt, interviewService.findById(id));
        interviewService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private Long requiredCompany(Jwt jwt) {
        Object claim = jwt == null ? null : jwt.getClaim("companyId");
        if (claim instanceof Number n) return n.longValue();
        if (claim instanceof String value) try { return Long.valueOf(value); } catch (NumberFormatException ignored) { }
        throw new AccessDeniedException("No company is associated with this account");
    }
    private void ensureTenant(Jwt jwt, InterviewDto interview) {
        if (!hasRole(jwt, "ADMIN") && !hasRole(jwt, "HR") && !hasRole(jwt, "MANAGER")) return;
        if (!isAdmin(jwt) && (interview.getCompanyId() == null || !interview.getCompanyId().equals(requiredCompany(jwt)))) throw new AccessDeniedException("Cross-company access denied");
    }
    @SuppressWarnings("unchecked")
    private boolean isAdmin(Jwt jwt) {
        return hasRole(jwt, "ADMIN");
    }
    @SuppressWarnings("unchecked")
    private boolean hasRole(Jwt jwt, String role) {
        if (jwt == null || jwt.getClaim("realm_access") == null) return false;
        Object roles = ((java.util.Map<String, Object>) jwt.getClaim("realm_access")).get("roles");
        return roles instanceof java.util.Collection<?> values && values.contains(role);
    }
}
