package com.ebanking.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
  private Long id;
  private String accountNumber;
  private String iban; // IBAN pour SEPA/Instant transfers
  private String userId;
  private BigDecimal balance;
  private String currency;
  private String type;
  private String status;
  private String nickname;
  private LocalDateTime createdAt;
}
