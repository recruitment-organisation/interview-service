package recruitment.dev.interviewservice.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignBearerTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (template.headers().containsKey("Authorization")) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            template.header("Authorization", "Bearer " + jwtAuthentication.getToken().getTokenValue());
        }
    }
}
