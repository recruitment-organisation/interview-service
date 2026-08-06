package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowClientFallbackFactory implements FallbackFactory<WorkflowClient> {

    @Override
    public WorkflowClient create(Throwable cause) {
        return (request, authorization) -> {
            throw InterviewFeignFallbacks.unavailable("workflow-service", cause);
        };
    }
}
