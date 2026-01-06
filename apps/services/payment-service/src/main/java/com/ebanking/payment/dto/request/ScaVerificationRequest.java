package com.ebanking.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScaVerificationRequest {
  @NotBlank private String otpCode;
}
