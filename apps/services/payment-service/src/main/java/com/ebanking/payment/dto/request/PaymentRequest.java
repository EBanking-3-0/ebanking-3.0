package com.ebanking.payment.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
  @NotNull(message = "From account ID is required")
  private Long fromAccountId;

  // Beneficiary Target (Generic)
  private Long toAccountId;
  private String toAccountNumber;
  private String toIban;
  private String beneficiaryName;
  private String beneficiarySwiftBic;

  // Telco specific
  private String phoneNumber;
  private String countryCode;

  // Merchant specific
  private String merchantId;
  private String invoiceReference;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  @Pattern(regexp = "^[A-Z]{3}$", message = "Invalid currency format")
  private String currency;

  @NotBlank(message = "Type is required")
  private String type; // INTERNAL_TRANSFER, SEPA_TRANSFER, SCT_INSTANT, SWIFT_TRANSFER,

  // MERCHANT_PAYMENT, MOBILE_RECHARGE

  @Size(max = 500, message = "Description too long")
  private String description;

  private String endToEndId; // ISO 20022 reference

  @NotBlank(message = "Idempotency key is required")
  private String idempotencyKey;

  private String ipAddress;
  private String userAgent;
}
