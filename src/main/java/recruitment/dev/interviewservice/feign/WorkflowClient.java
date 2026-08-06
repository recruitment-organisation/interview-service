package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import recruitment.dev.interviewservice.dto.WorkflowCompleteTaskRequest;

@FeignClient(
        name = "workflow-service",
        fallbackFactory = WorkflowClientFallbackFactory.class
)
public interface WorkflowClient {

    @PostMapping("/workflow-tasks/complete")
    void completeTask(
            @RequestBody WorkflowCompleteTaskRequest request,
            @RequestHeader("Authorization") String authorization
    );
}
