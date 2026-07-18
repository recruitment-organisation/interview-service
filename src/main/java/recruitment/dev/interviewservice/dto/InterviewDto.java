package recruitment.dev.interviewservice.dto;



import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.entities.InterviewType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDto {

    private Long id;

    @NotNull
    private Long applicationId;

    @NotNull
    private Long interviewerId;

    @NotNull
    @Future

    private LocalDateTime scheduledAt;

    @NotNull
    @Min(15)
    private Integer duration;

    @NotNull
    private InterviewType type;

    private InterviewStatus status;

    private String meetingLink;

    private String location;

    @Size(max = 3000)
    private String notes;

    @Size(max = 3000)
    private String feedback;

    private InterviewResult result;
}