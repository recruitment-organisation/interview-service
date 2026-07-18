package recruitment.dev.interviewservice.exception;

public class InterviewNotFoundException extends RuntimeException {
    public InterviewNotFoundException(Long id) {
        super("Interview introuvable avec l'id : " + id);
    }

    public InterviewNotFoundException(String message) {
        super(message);
    }
}
