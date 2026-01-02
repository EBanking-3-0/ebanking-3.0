package com.ebanking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user contact information management.
 * Used to update email, phone, and push token in notification preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContactDTO {

  private String emailAddress;
  private String phoneNumber;
  private String pushToken;
}
