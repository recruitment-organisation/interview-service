package recruitment.dev.interviewservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.entities.InterviewStatus;

import java.util.Collection;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    Page<Interview> findByApplicationId(Long applicationId, Pageable pageable);
    Page<Interview> findByInterviewerId(Long interviewerId, Pageable pageable);
    Page<Interview> findByStatus(InterviewStatus status, Pageable pageable);
    Page<Interview> findByCompanyId(Long companyId, Pageable pageable);
    Page<Interview> findByCompanyIdAndStatus(Long companyId, InterviewStatus status, Pageable pageable);
    Page<Interview> findByCompanyIdAndApplicationId(Long companyId, Long applicationId, Pageable pageable);
    Page<Interview> findByCompanyIdAndInterviewerId(Long companyId, Long interviewerId, Pageable pageable);
    List<Interview> findByInterviewerIdAndStatusIn(Long interviewerId, Collection<InterviewStatus> statuses);
}
