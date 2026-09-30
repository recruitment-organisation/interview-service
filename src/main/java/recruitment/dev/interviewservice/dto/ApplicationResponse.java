package recruitment.dev.interviewservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationResponse {

    private Long id;

    private Long candidateId;

    private Long jobOfferId;
    private Long companyId;

    private String status;

    private String currentTaskId;

    private String currentTaskDefinitionKey;

    private String currentTaskName;
}
