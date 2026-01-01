package com.ebanking.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String transactionId; // Reference unique plateforme

  @Column(unique = true, nullable = false)
  private String idempotencyKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentType paymentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  // Account details
  @Column(nullable = false)
  private Long fromAccountId;

  private String fromIban;

  private Long toAccountId;
  private String toIban;
  private String beneficiaryName;
  private String beneficiaryAddress;
  private String beneficiarySwiftBic;

  // Amount & Currency
  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Builder.Default
  @Column(nullable = false)
  private String currency = "EUR";

  // Fees & FX
  @Builder.Default private BigDecimal fees = BigDecimal.ZERO;
  private BigDecimal exchangeRate;
  private String sourceCurrency;
  private String targetCurrency;

  // Banking Dates
  private LocalDate valueDate;
  private LocalDate accountingDate;

  // Metadata & Tracking
  @Column(nullable = false)
  private Long userId;

  private String description;
  private String reference; // Customer reference (EndToEndId in ISO)
  private String uetr; // Unique End-to-end Transaction Reference (SWIFT)

  // External ids
  private String debitTransactionId;
  private String creditTransactionId;
  private String externalTransactionId;
  private String iso20022MessageReference;

  // Telco specific
  private String phoneNumber;
  private String operatorCode;

  // Security & Compliance
  @Builder.Default private Boolean scaVerified = false;
  @Builder.Default private Boolean fraudCheckPassed = false;
  private String amlScreeningStatus; // PENDING, CLEAR, FLAGGED
  private String riskScore;

  // Context
  private String ipAddress;
  private String userAgent;
  private String failureReason;

  private Instant estimatedCompletionDate;
  private Instant completedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
    if (valueDate == null) valueDate = LocalDate.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}
