package recruitment.dev.interviewservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import recruitment.dev.interviewservice.entities.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Page<Interview> findByApplicationId(Long applicationId, Pageable pageable);
    Page<Interview> findByInterviewerId(Long interviewerId, Pageable pageable);
}
