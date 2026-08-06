package recruitment.dev.interviewservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid interview request");
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ProblemDetail handleMalformedRequest(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request", "One or more request values are invalid");
    }

    @ExceptionHandler({ResourceNotFoundException.class, InterviewNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return problem(HttpStatus.NOT_FOUND, "Interview not found", exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Business rule violation", exception.getMessage());
    }

    @ExceptionHandler({ConflictException.class, OptimisticLockingFailureException.class})
    public ProblemDetail handleConflict(Exception exception) {
        return problem(HttpStatus.CONFLICT, "Interview conflict", exception.getMessage());
    }

    @ExceptionHandler(DependencyUnavailableException.class)
    public ProblemDetail handleDependencyUnavailable(DependencyUnavailableException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Dependency unavailable", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
