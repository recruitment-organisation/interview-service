package recruitment.dev.interviewservice.feign;

import recruitment.dev.interviewservice.exception.DependencyUnavailableException;

final class InterviewFeignFallbacks {

    private InterviewFeignFallbacks() {
    }

    static DependencyUnavailableException unavailable(String dependency, Throwable cause) {
        return new DependencyUnavailableException("Required dependency is unavailable: " + dependency, cause);
    }
}
