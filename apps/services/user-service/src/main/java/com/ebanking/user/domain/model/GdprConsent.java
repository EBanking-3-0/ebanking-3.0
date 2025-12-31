package com.ebanking.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "gdpr_consents", schema = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GdprConsent {

  @Id @GeneratedValue private UUID id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // Changed from userProfile

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConsentType consentType;

  @Column(nullable = false)
  @Builder.Default
  private boolean granted = false;

  private LocalDateTime grantedAt;
  private LocalDateTime revokedAt;

  @Builder.Default private String consentVersion = "v1.0";

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  public enum ConsentType {
    MARKETING_EMAIL,
    MARKETING_SMS,
    MARKETING_PHONE,
    PERSONALIZED_OFFERS,
    DATA_SHARING_PARTNERS,
    ANALYTICS_IMPROVEMENT,
    OPEN_BANKING_SHARING
  }
}
