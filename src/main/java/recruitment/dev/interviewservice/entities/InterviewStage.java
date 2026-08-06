package recruitment.dev.interviewservice.entities;

import java.util.Optional;

public enum InterviewStage {
    HR_INTERVIEW("hrInterview", "hrApproved"),
    TECHNICAL_INTERVIEW("technicalInterview", "technicalApproved"),
    MANAGER_INTERVIEW("managerInterview", "managerApproved");

    private final String taskDefinitionKey;
    private final String workflowVariable;

    InterviewStage(String taskDefinitionKey, String workflowVariable) {
        this.taskDefinitionKey = taskDefinitionKey;
        this.workflowVariable = workflowVariable;
    }

    public String taskDefinitionKey() {
        return taskDefinitionKey;
    }

    public String workflowVariable() {
        return workflowVariable;
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
