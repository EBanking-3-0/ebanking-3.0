package com.ebanking.notification.client;

import com.ebanking.notification.dto.UserContactDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for communicating with User Service. Uses service discovery (Eureka) and circuit
 * breaker pattern for resilience.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

  private final RestTemplate restTemplate;

  @Value("${user-service.base-url:http://user-service}")
  private String userServiceBaseUrl;

  /**
   * Fetch user contact information from user-service.
   *
   * @param userId User ID
   * @return User contact information
   */
  public UserContactDTO getUserContact(String userId) {
    try {
      String url = userServiceBaseUrl + "/api/users/{userId}/contact";
      UserContactDTO response = restTemplate.getForObject(url, UserContactDTO.class, userId);

      if (response != null) {
        log.debug("Retrieved user contact for userId: {}", userId);
        return response;
      }

      log.warn("User service returned null for userId: {}", userId);
      throw new UserServiceException("User not found: " + userId);

    } catch (Exception e) {
      log.error("Failed to fetch user contact from user-service for userId: {}", userId, e);
      throw new UserServiceException("Failed to fetch user contact: " + e.getMessage(), e);
    }
  }

  /**
   * Check if user exists.
   *
   * @param userId User ID
   * @return true if user exists
   */
  public boolean userExists(String userId) {
    try {
      String url = userServiceBaseUrl + "/api/users/{userId}/exists";
      Boolean response = restTemplate.getForObject(url, Boolean.class, userId);
      return response != null && response;
    } catch (Exception e) {
      log.warn("Failed to check user existence for userId: {}", userId, e);
      return false;
    }
  }

  /** Custom exception for user service errors. */
  public static class UserServiceException extends RuntimeException {

    public UserServiceException(String message) {
      super(message);
    }

    public UserServiceException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
