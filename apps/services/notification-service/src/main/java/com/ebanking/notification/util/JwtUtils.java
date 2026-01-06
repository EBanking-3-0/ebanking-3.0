package com.ebanking.notification.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting user information from JWT tokens. Handles different JWT claim
 * structures from various identity providers.
 */
@Slf4j
@Component
public class JwtUtils {

  /**
   * Extract user ID from JWT authentication token. Tries multiple claim names in order: 1. userId
   * (custom claim) 2. sub (subject - standard JWT claim)
   *
   * @param authentication Spring Security Authentication object
   * @return User ID as Long, or null if not found or invalid
   */
  public Long extractUserId(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      log.warn("Authentication is null or principal is not a JWT");
      return null;
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();

    // Try userId claim first (custom claim from Keycloak/Auth service)
    Object userIdClaim = jwt.getClaim("userId");
    if (userIdClaim != null) {
      return convertToLong(userIdClaim);
    }

    // Try user_id claim (alternative format)
    userIdClaim = jwt.getClaim("user_id");
    if (userIdClaim != null) {
      return convertToLong(userIdClaim);
    }

    // Fall back to sub (subject) claim
    String sub = jwt.getSubject();
    if (sub != null) {
      Long userId = convertToLong(sub);
      if (userId != null) {
        return userId;
      }

      // If sub is a UUID string, generate stable numeric ID
      log.debug("Converting UUID sub to numeric userId: {}", sub);
      return (long) Math.abs(sub.hashCode());
    }

    log.warn("Could not extract userId from JWT token");
    return null;
  }

  /**
   * Extract username from JWT authentication token.
   *
   * @param authentication Spring Security Authentication object
   * @return Username, or null if not found
   */
  public String extractUsername(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      return null;
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();

    // Try preferred_username claim (Keycloak standard)
    String username = jwt.getClaim("preferred_username");
    if (username != null) {
      return username;
    }

    // Try name claim
    username = jwt.getClaim("name");
    if (username != null) {
      return username;
    }

    // Fall back to sub
    return jwt.getSubject();
  }

  /**
   * Extract email from JWT authentication token.
   *
   * @param authentication Spring Security Authentication object
   * @return Email address, or null if not found
   */
  public String extractEmail(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      return null;
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getClaim("email");
  }

  /**
   * Convert object to Long, handling various numeric types.
   *
   * @param value Object to convert
   * @return Long value, or null if conversion fails
   */
  private Long convertToLong(Object value) {
    if (value == null) {
      return null;
    }

    try {
      if (value instanceof Long) {
        return (Long) value;
      } else if (value instanceof Integer) {
        return ((Integer) value).longValue();
      } else if (value instanceof String) {
        return Long.valueOf((String) value);
      } else if (value instanceof Number) {
        return ((Number) value).longValue();
      }
    } catch (NumberFormatException e) {
      log.debug("Failed to convert value to Long: {}", value);
    }

    return null;
  }
}
