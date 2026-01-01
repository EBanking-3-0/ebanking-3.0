package com.ebanking.payment.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerificationResponse {
  private boolean verified;
  private String message;
  private String errorCode;
}
