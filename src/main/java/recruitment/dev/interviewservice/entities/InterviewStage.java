package recruitment.dev.interviewservice.entities;

import java.util.Optional;

public enum InterviewStage {
    HR_INTERVIEW("hrInterview", "hrApproved", "hrComment"),
    TECHNICAL_INTERVIEW("technicalInterview", "technicalApproved", "technicalComment"),
    MANAGER_INTERVIEW("managerInterview", "managerApproved", "managerComment");

    private final String taskDefinitionKey;
    private final String workflowVariable;
    private final String commentVariable;

    InterviewStage(String taskDefinitionKey, String workflowVariable, String commentVariable) {
        this.taskDefinitionKey = taskDefinitionKey;
        this.workflowVariable = workflowVariable;
        this.commentVariable = commentVariable;
    }

    public String taskDefinitionKey() {
        return taskDefinitionKey;
    }

    public String workflowVariable() {
        return workflowVariable;
    }

    public String commentVariable() {
        return commentVariable;
    }

    public static Optional<InterviewStage> fromTaskDefinitionKey(String taskDefinitionKey) {
        if (taskDefinitionKey == null) {
            return Optional.empty();
        }
        for (InterviewStage stage : values()) {
            if (stage.taskDefinitionKey.equals(taskDefinitionKey)) {
                return Optional.of(stage);
            }
        }
        return Optional.empty();
    }
}
