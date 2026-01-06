package com.ebanking.graphql.model;

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
  private Long fromAccountId;
  private Long toAccountId;
  private String toAccountNumber;
  private String toIban;
  private String beneficiaryName;
  private String beneficiarySwiftBic;
  private String phoneNumber;
  private String countryCode;
  private String merchantId;
  private String invoiceReference;
  private BigDecimal amount;
  private String currency;
  private String type;
  private String description;
  private String endToEndId;
  private String idempotencyKey;
  private String ipAddress;
  private String userAgent;
}
