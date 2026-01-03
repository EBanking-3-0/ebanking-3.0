package com.ebanking.account.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "accounts",
    indexes = {
      @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
      @Index(name = "idx_user_id", columnList = "userId")
    })
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String accountNumber;

  @Column private String iban; // IBAN pour SEPA/Instant transfers

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private BigDecimal balance;

  @Column(nullable = false)
  private String currency;

  @Column(nullable = false)
  private String type; // SAVINGS, CHECKING

  @Column(nullable = false)
  private String status; // ACTIVE, FROZEN, CLOSED

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
