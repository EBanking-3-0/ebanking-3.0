package com.ebanking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
