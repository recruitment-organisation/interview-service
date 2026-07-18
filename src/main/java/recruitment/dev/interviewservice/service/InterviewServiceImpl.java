package recruitment.dev.interviewservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.Interview;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;

import recruitment.dev.interviewservice.exception.BusinessException;
import recruitment.dev.interviewservice.exception.ResourceNotFoundException;
import recruitment.dev.interviewservice.mapper.InterviewMapper;
import recruitment.dev.interviewservice.repository.InterviewRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {


    private final InterviewRepository repository;
    private final InterviewMapper mapper;


    @Override
    public InterviewDto create(InterviewDto dto) {
        validateInterview(dto);
        Interview interview = mapper.toEntity(dto);

        Interview saved = repository.save(interview);

        return mapper.toDto(saved);
    }


    @Override
    public InterviewDto update(Long id, InterviewDto dto) {
        validateInterview(dto);
        Interview interview = getInterview(id);
        mapper.updateEntity(dto, interview);




        return mapper.toDto(repository.save(interview));
    }


    @Override
    @Transactional(readOnly = true)
    public InterviewDto findById(Long id) {

        return mapper.toDto(getInterview(id));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findAll(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findByApplicationId(Long applicationId,
                                                  Pageable pageable) {

        return repository.findByApplicationId(applicationId,pageable)
                .map(mapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<InterviewDto> findByInterviewerId(Long interviewerId,
                                                  Pageable pageable) {

        return repository.findByInterviewerId(interviewerId, pageable)
                .map(mapper::toDto);
    }


    @Override
    public void delete(Long id) {

        Interview interview = getInterview(id);

        repository.delete(interview);
    }


    @Override
    public InterviewDto updateStatus(Long id,
                                     InterviewStatus status) {

        Interview interview = getInterview(id);

        interview.setStatus(status);

        return mapper.toDto(repository.save(interview));
    }


    @Override
    public InterviewDto addFeedback(Long id,
                                    String feedback,
                                    String notes,
                                    InterviewResult result) {

        Interview interview = getInterview(id);

        interview.setFeedback(feedback);
        interview.setNotes(notes);
        interview.setResult(result);
        interview.setStatus(InterviewStatus.COMPLETED);


        return mapper.toDto(repository.save(interview));
    }


    private Interview getInterview(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Interview not found : " + id));
    }
    private void validateInterview(InterviewDto dto){

        if(dto.getDuration() <=0){
            throw new BusinessException(
                    "Duration must be positive"
            );
        }

        if(dto.getScheduledAt()
                .isBefore(LocalDateTime.now())){
            throw new BusinessException(
                    "Interview date cannot be in the past"
            );
        }
    }
}