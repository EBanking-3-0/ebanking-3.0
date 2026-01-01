package com.ebanking.payment.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerificationRequest {
  private Long userId;
  private Long paymentId;
  private String mfaCode;
  private String mfaType; // SMS, EMAIL, TOTP
}
