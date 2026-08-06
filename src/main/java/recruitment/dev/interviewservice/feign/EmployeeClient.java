package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import recruitment.dev.interviewservice.dto.EmployeeResponse;

@FeignClient(
        name = "employee-service",
        fallbackFactory = EmployeeClientFallbackFactory.class
)
public interface EmployeeClient {

    @GetMapping("/employee/get/{id}")
    EmployeeResponse getEmployeeById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/employee/me")
    EmployeeResponse getCurrentEmployee(@RequestHeader("Authorization") String authorization);

}
