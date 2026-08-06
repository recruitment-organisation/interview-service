package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import recruitment.dev.interviewservice.dto.EmployeeResponse;

@Component
public class EmployeeClientFallbackFactory implements FallbackFactory<EmployeeClient> {

    @Override
    public EmployeeClient create(Throwable cause) {
        return new EmployeeClient() {
            @Override
            public EmployeeResponse getEmployeeById(Long id, String authorization) {
                throw InterviewFeignFallbacks.unavailable("employee-service", cause);
            }

            @Override
            public EmployeeResponse getCurrentEmployee(String authorization) {
                throw InterviewFeignFallbacks.unavailable("employee-service", cause);
            }
        };
    }
}
