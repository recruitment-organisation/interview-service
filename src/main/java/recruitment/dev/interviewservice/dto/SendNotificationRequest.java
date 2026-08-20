package recruitment.dev.interviewservice.dto;

public record SendNotificationRequest(
        Long candidateId,
        String candidateKeycloakId,
        Long applicationId,
        String recipientEmail,
        String type,
        String subject,
        String message
) {
}
