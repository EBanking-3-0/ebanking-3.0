package com.ebanking.graphql.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Try to get the authentication from SecurityContext
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String token = null;

                System.out.println("DEBUG: Authentication type: " + (authentication != null ? authentication.getClass().getName() : "null"));
                
                if (authentication instanceof JwtAuthenticationToken jwtToken) {
                    Jwt jwt = jwtToken.getToken();
                    token = jwt.getTokenValue();
                    System.out.println("DEBUG: Got token from JwtAuthenticationToken");
                } else if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
                    token = jwt.getTokenValue();
                    System.out.println("DEBUG: Got token from credentials");
                }
                
                // Fallback: Try to get the token from the current HTTP request
                if (token == null) {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        String authHeader = request.getHeader("Authorization");
                        System.out.println("DEBUG: Authorization header from request: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                            System.out.println("DEBUG: Got token from HTTP request header");
                        }
                    } else {
                        System.out.println("DEBUG: RequestAttributes is null");
                    }
                }

                if (token != null) {
                    template.header("Authorization", "Bearer " + token);
                    System.out.println("DEBUG: Added Authorization header to Feign request");
                } else {
                    System.out.println("DEBUG: No token found - request will be sent without Authorization header");
                }
            }
        };
    }
}
