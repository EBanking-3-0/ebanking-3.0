package com.ebanking.payment.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalTransferRequest {

  @NotNull(message = "From account ID is required")
  private Long fromAccountId;

  private Long toAccountId;

  private String toAccountNumber;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency format")
  private String currency;

  @Size(max = 500, message = "Description too long")
  private String description;

  @NotBlank(message = "Idempotency key is required")
  private String idempotencyKey;

  private String ipAddress;
  private String userAgent;
}
