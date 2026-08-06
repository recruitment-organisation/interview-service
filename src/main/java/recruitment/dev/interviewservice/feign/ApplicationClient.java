package recruitment.dev.interviewservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import recruitment.dev.interviewservice.dto.ApplicationResponse;

@FeignClient(
        name = "application-service",
        fallbackFactory = ApplicationClientFallbackFactory.class
)
public interface ApplicationClient {

    @GetMapping("/applications/get/{id}")
    ApplicationResponse getApplicationById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorization
    );

}
