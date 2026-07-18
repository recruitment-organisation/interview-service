package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import recruitment.dev.interviewservice.dto.ApplicationResponse;

@FeignClient(name = "application-service")
public interface ApplicationClient {

    @GetMapping("/api/applications/{id}")
    ApplicationResponse getApplicationById(@PathVariable Long id);

}