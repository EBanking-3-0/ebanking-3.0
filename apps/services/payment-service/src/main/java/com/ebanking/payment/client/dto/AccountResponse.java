package com.ebanking.payment.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
  private Long id;
  private String accountNumber;
  private String iban; // Added for SEPA/Instant transfers
  private Long userId;
  private BigDecimal balance;
  private String currency;
  private String type;
  private String status;
  private LocalDateTime createdAt;
}
