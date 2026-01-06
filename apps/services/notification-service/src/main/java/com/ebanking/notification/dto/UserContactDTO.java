package com.ebanking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user contact information. Used to fetch user contact details from user service. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContactDTO {
  private String userId;
  private String email;
  private String phoneNumber;
  private String firstName;
  private String lastName;
  private String preferredLanguage;
}
