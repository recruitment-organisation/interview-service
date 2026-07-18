package recruitment.dev.interviewservice.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import recruitment.dev.interviewservice.dto.InterviewDto;
import recruitment.dev.interviewservice.entities.InterviewResult;
import recruitment.dev.interviewservice.entities.InterviewStatus;
import recruitment.dev.interviewservice.service.InterviewService;


@RestController
@RequestMapping("/interviews")
@RequiredArgsConstructor
public class InterviewController {


    private final InterviewService interviewService;


    @PostMapping
    public ResponseEntity<InterviewDto> create(
            @Valid @RequestBody InterviewDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(interviewService.create(dto));
    }


    @PutMapping("/{id}")
    public ResponseEntity<InterviewDto> update(
            @PathVariable Long id,
            @Valid @RequestBody InterviewDto dto) {

        return ResponseEntity.ok(
                interviewService.update(id, dto)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<InterviewDto> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                interviewService.findById(id)
        );
    }


    @GetMapping
    public ResponseEntity<Page<InterviewDto>> findAll(
            Pageable pageable) {

        return ResponseEntity.ok(
                interviewService.findAll(pageable)
        );
    }


    @GetMapping("/application/{applicationId}")
    public ResponseEntity<Page<InterviewDto>> findByApplication(
            @PathVariable Long applicationId,
            Pageable pageable) {

        return ResponseEntity.ok(
                interviewService.findByApplicationId(
                        applicationId,
                        pageable
                )
        );
    }


    @GetMapping("/interviewer/{interviewerId}")
    public ResponseEntity<Page<InterviewDto>> findByInterviewer(
            @PathVariable Long interviewerId,
            Pageable pageable) {

        return ResponseEntity.ok(
                interviewService.findByInterviewerId(
                        interviewerId,
                        pageable
                )
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<InterviewDto> updateStatus(
            @PathVariable Long id,
            @RequestParam InterviewStatus status) {

        return ResponseEntity.ok(
                interviewService.updateStatus(id, status)
        );
    }


    @PatchMapping("/{id}/feedback")
    public ResponseEntity<InterviewDto> addFeedback(
            @PathVariable Long id,
            @RequestParam String feedback,
            @RequestParam(required = false) String notes,
            @RequestParam InterviewResult result) {

        return ResponseEntity.ok(
                interviewService.addFeedback(
                        id,
                        feedback,
                        notes,
                        result
                )
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        interviewService.delete(id);

        return ResponseEntity.noContent().build();
    }
}