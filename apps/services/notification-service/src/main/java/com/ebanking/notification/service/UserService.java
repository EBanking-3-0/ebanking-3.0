package com.ebanking.notification.service;

import com.ebanking.notification.client.UserServiceClient;
import com.ebanking.notification.dto.UserContactDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for user-related operations. Delegates to UserServiceClient for REST calls to
 * user-service via service discovery and circuit breaker pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserServiceClient userServiceClient;

  /**
   * Get user contact information from user-service.
   *
   * @param userId User ID
   * @return User contact DTO
   */
  public UserContactDTO getUserContact(String userId) {
    return userServiceClient.getUserContact(userId);
  }

  /**
   * Check if user exists.
   *
   * @param userId User ID
   * @return true if user exists
   */
  public boolean userExists(String userId) {
    return userServiceClient.userExists(userId);
  }
}
