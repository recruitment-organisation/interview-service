package recruitment.dev.interviewservice.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long interviewerId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

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

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = InterviewStatus.SCHEDULED;
        }
    }
}