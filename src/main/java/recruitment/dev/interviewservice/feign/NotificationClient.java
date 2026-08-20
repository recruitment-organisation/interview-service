package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import recruitment.dev.interviewservice.dto.SendNotificationRequest;

@FeignClient(
        name = "notification-service",
        fallbackFactory = NotificationClientFallbackFactory.class
)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody SendNotificationRequest request);
}
