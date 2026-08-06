package recruitment.dev.interviewservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.entities.InterviewType;

import java.time.LocalDateTime;

public interface InterviewService {
    InterviewDto create(InterviewDto dto);

    InterviewDto update(Long id, InterviewDto dto);

    InterviewDto findById(Long id);

    Page<InterviewDto> findAll(Pageable pageable);

    Page<InterviewDto> findByApplicationId(Long applicationId, Pageable pageable);

    Page<InterviewDto> findByInterviewerId(Long interviewerId, Pageable pageable);

    Page<InterviewDto> findByStatus(InterviewStatus status, Pageable pageable);

    void delete(Long id);

    InterviewDto updateStatus(Long id, InterviewStatus status);

    InterviewDto addFeedback(Long id,
                             String feedback,
                             String notes,
                             Boolean approved,
                             InterviewResult result);
}
