package recruitment.dev.interviewservice.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews", indexes = {
        @Index(name = "idx_interview_application", columnList = "application_id"),
        @Index(name = "idx_interview_status", columnList = "status"),
        @Index(name = "idx_interview_interviewer_status_scheduled", columnList = "interviewer_id,status,scheduled_at")
})
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "interviewer_id", nullable = false)
    private Long interviewerId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private InterviewStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    private String meetingLink;

    private String location;

    @Column(length = 3000)
    private String notes;

    @Column(length = 3000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    private InterviewResult result;

    @Column(name = "approved")
    private Boolean approved;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = InterviewStatus.SCHEDULED;
        }
    }
}
