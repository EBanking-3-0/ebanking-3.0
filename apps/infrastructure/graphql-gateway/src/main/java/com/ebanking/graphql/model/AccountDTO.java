package com.ebanking.graphql.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private String id; // GraphQL ID is string usually
    private String accountNumber;
    private String userId;
    private BigDecimal balance;
    private String currency;
    private String type;
    private String status;
    private String createdAt; // Keep as string for simplicity with GraphQL default scalars
}
