package recruitment.dev.interviewservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.Interview;

@Mapper(componentModel = "spring")
public interface InterviewMapper {
    InterviewDto toDto(Interview interview) ;

    Interview toEntity(InterviewDto interviewDto)
            ;
    void updateEntity(InterviewDto dto,
                      @MappingTarget Interview entity);
}


