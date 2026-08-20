package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {

    @Override
    public NotificationClient create(Throwable cause) {
        return request -> {
            throw InterviewFeignFallbacks.unavailable("notification-service", cause);
        };
    }
}
