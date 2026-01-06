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
   * Extract user ID from JWT authentication token.
   *
   * @param authentication Spring Security Authentication object
   * @return User ID as String (UUID), or null if not found
   */
  public String extractUserId(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      log.warn("Authentication is null or principal is not a JWT");
      return null;
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();

    // Try sub (subject) claim first - this is the standard Keycloak ID
    String sub = jwt.getSubject();
    if (sub != null) {
      return sub;
    }

    // Try userId claim
    Object userIdClaim = jwt.getClaim("userId");
    if (userIdClaim != null) {
      return userIdClaim.toString();
    }

    // Try user_id claim
    userIdClaim = jwt.getClaim("user_id");
    if (userIdClaim != null) {
      return userIdClaim.toString();
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
}
