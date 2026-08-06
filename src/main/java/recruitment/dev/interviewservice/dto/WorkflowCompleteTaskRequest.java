package recruitment.dev.interviewservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WorkflowCompleteTaskRequest {

    @NotBlank(message = "taskId is required")
    private String taskId;

    private Map<String, Object> variables = new HashMap<>();
}
