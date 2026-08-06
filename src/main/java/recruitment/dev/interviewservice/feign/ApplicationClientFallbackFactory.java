package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import recruitment.dev.interviewservice.dto.ApplicationResponse;

@Component
public class ApplicationClientFallbackFactory implements FallbackFactory<ApplicationClient> {

    @Override
    public ApplicationClient create(Throwable cause) {
        return (id, authorization) -> {
            throw InterviewFeignFallbacks.unavailable("application-service", cause);
        };
    }
}
