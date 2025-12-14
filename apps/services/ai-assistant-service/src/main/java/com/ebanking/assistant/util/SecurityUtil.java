package com.ebanking.assistant.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting user information from JWT tokens.
 * In production, use a proper JWT library like jjwt or spring-security-jwt.
 */
@Slf4j
@Component
public class SecurityUtil {
    
    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*(\\d+)");
    
    /**
     * Extract user ID from JWT token in Authorization header.
     * This is a simplified implementation - in production, properly validate and decode JWT.
     */
    public Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found");
            return null;
        }
        
        String token = authHeader.substring(7);
        try {
            // Decode JWT payload (simplified - in production, use proper JWT library)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return null;
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Matcher matcher = USER_ID_PATTERN.matcher(payload);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
            
            // Fallback: try to extract from "sub" claim or other fields
            if (payload.contains("\"sub\"")) {
                Pattern subPattern = Pattern.compile("\"sub\"\\s*:\\s*\"(\\d+)\"");
                Matcher subMatcher = subPattern.matcher(payload);
                if (subMatcher.find()) {
                    return Long.parseLong(subMatcher.group(1));
                }
            }
            
            log.warn("Could not extract userId from JWT token");
            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
            return null;
        }
    }
    
    /**
     * For testing/development: allow userId to be passed as a header
     */
    public Long extractUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            try {
                return Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userIdHeader);
            }
        }
        return extractUserId(request);
    }
}
