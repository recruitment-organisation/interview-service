package recruitment.dev.interviewservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.Interview;

@Mapper(componentModel = "spring")
public interface InterviewMapper {
    InterviewDto toDto(Interview interview) ;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    Interview toEntity(InterviewDto interviewDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "feedback", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    void updateEntity(InterviewDto dto,
                      @MappingTarget Interview entity);
}
