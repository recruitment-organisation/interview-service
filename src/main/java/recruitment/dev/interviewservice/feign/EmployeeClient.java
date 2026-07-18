package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import recruitment.dev.interviewservice.dto.EmployeeResponse;

@FeignClient(name = "employee-service")
public interface EmployeeClient {

    @GetMapping("/api/employees/{id}")
    EmployeeResponse getEmployeeById(@PathVariable Long id);

}