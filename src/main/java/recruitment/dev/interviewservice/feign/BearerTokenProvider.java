package recruitment.dev.interviewservice.feign;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Captures the bearer token on the request thread before a Feign circuit-breaker
 * switches execution to a worker thread.
 */
@Component
public class BearerTokenProvider {

    public String currentAuthorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return "Bearer " + jwtAuthentication.getToken().getTokenValue();
        }
        throw new IllegalStateException("A JWT authentication is required for downstream service calls");
    }
}
