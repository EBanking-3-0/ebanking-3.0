package com.ebanking.assistant.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting user information from JWT tokens.
 * In production, use a proper JWT library like jjwt or spring-security-jwt.
 */
@Slf4j
@Component
public class SecurityUtil {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * Extract user ID from JWT token in Authorization header.
     * Extracts the 'sub' claim which contains the user's UUID.
     */
    public String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found");
            return null;
        }
        
        String token = authHeader.substring(7);
        try {
            log.debug("Auth header present, token length={}", token.length());
            // Decode JWT payload (simplified - in production, use proper JWT library)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token format");
                return null;
            }
            
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            log.debug("JWT header: {}", headerJson);
            log.debug("JWT payload: {}", payloadJson);

            Map<String, Object> claims = OBJECT_MAPPER.readValue(payloadJson, new TypeReference<>() {});
            log.debug("JWT claims keys: {}", claims.keySet());

            // Extract sub claim (UUID) as userId
            String userId = extractStringClaim(claims.get("sub"));
            log.debug("Resolved sub claim as userId: {}", userId);
            if (userId != null) {
                return userId;
            }

            // Fallback to preferred_username if sub is not available
            userId = extractStringClaim(claims.get("preferred_username"));
            log.debug("Resolved preferred_username as userId: {}", userId);
            if (userId != null) {
                return userId;
            }

            log.warn("Could not extract userId from JWT token");
            return null;
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
            return null;
        }
    }

    private String extractStringClaim(Object claim) {
        if (claim == null) {
            return null;
        }

        if (claim instanceof String stringValue && StringUtils.hasText(stringValue)) {
            return stringValue.trim();
        }

        return claim.toString();
    }
    
    /**
     * For testing/development: allow userId to be passed as a header
     */
    public String extractUserIdFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            return userIdHeader;
        }
        return extractUserId(request);
    }
}
