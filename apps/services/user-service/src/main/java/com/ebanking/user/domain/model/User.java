package com.ebanking.user.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import lombok.*;

/**
 * User - Entité de domaine représentant un utilisateur du système e-banking
 *
 * <p>Un utilisateur est créé lors de la première soumission du KYC et reste lié au compte Keycloak
 * via keycloakId.
 *
 * <p>Statuts possibles: - PENDING_REVIEW: Création initiale, en attente d'approbation KYC - ACTIVE:
 * KYC approuvé, utilisateur actif et autorisé à utiliser le service - REJECTED: KYC rejeté,
 * utilisateur ne peut pas utiliser le service
 *
 * <p>Relations: - OneToOne avec KycVerification: Vérification KYC de l'utilisateur - OneToMany avec
 * GdprConsent: Consentements GDPR de l'utilisateur
 */
@Entity
@Table(name = "users", schema = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  /** UUID unique généré automatiquement */
  @Id @GeneratedValue private UUID id;

  /**
   * Identifiant Keycloak unique et immuable Correspond à la revendication "sub" du JWT Lien
   * permanent vers le compte Keycloak
   */
  @Column(nullable = false, unique = true, updatable = false)
  private String keycloakId;

  /** Adresse email unique Extraite du JWT Keycloak lors de la création de l'utilisateur */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * Prénom de l'utilisateur Initialement extrait du JWT Keycloak Mis à jour lors de la soumission
   * du KYC avec les informations du formulaire
   */
  @Column(nullable = false)
  private String firstName;

  /**
   * Nom de famille de l'utilisateur Initialement extrait du JWT Keycloak Mis à jour lors de la
   * soumission du KYC avec les informations du formulaire
   */
  @Column(nullable = false)
  private String lastName;

  /** Numéro de téléphone Défini lors de la soumission du KYC */
  @Column(nullable = false)
  private String phone;

  /** Première ligne d'adresse Définie lors de la soumission du KYC */
  private String addressLine1;

  /** Deuxième ligne d'adresse (optionnelle) Définie lors de la soumission du KYC */
  private String addressLine2;

  /** Ville de résidence Définie lors de la soumission du KYC */
  private String city;

  /** Code postal Défini lors de la soumission du KYC */
  private String postalCode;

  /** Pays de résidence Défini lors de la soumission du KYC */
  private String country;

  /** Langue préférée de l'utilisateur pour les notifications et l'interface Par défaut: "en" */
  @Column(nullable = false)
  @Builder.Default
  private String preferredLanguage = "en";

  /**
   * Statut de l'utilisateur PENDING_REVIEW: En attente d'approbation KYC ACTIVE: Utilisateur actif
   * et autorisé REJECTED: Compte rejeté
   *
   * <p>Défini à PENDING_REVIEW lors de la création Mis à jour par un service d'approbation externe
   * après révision KYC
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private UserStatus status = UserStatus.PENDING_REVIEW;

  /** Timestamp de création Défini automatiquement à la création de l'enregistrement Immuable */
  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  /** Timestamp de dernière mise à jour Mis à jour automatiquement lors de chaque modification */
  @Column(name = "updated_at")
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  /**
   * Relation OneToMany vers la vérification KYC Relation unidirectionnelle depuis User vers
   * KycVerification - Cascade ALL: Les modifications sur User sont répercutées sur KycVerification
   * - Orphan removal: Si KycVerification est supprimée de User, elle est supprimée de la BD - Lazy
   * fetch: Chargement à la demande pour performance
   */
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("createdAt DESC") // la plus récente en premier
  @Builder.Default
  private List<KycVerification> kycVerifications = new ArrayList<>();

  /**
   * Relation OneToMany vers les consentements GDPR Collection des consentements accordés/refusés
   * par l'utilisateur - Cascade ALL: Les modifications sur User sont répercutées sur les
   * consentements - Orphan removal: Les consentements orphelins sont supprimés - Lazy fetch:
   * Chargement à la demande pour performance
   */
  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private Set<GdprConsent> consents = new HashSet<>();

  /** Énumération des statuts possibles d'un utilisateur */
  public enum UserStatus {
    /** En attente de révision KYC (statut initial) */
    PENDING_REVIEW,

    /** Utilisateur actif, KYC approuvé */
    ACTIVE,

    /** Utilisateur rejeté, KYC non approuvé */
    REJECTED
  }

  public void addConsent(GdprConsent consent) {
    if (this.consents == null) {
      this.consents = new HashSet<>();
    }
    this.consents.add(consent);
    consent.setUser(this);
  }

  public void removeConsent(GdprConsent consent) {
    if (this.consents != null) {
      this.consents.remove(consent);
      if (consent != null) {
        consent.setUser(null);
      }
    }
  }

  public void clearConsents() {
    if (this.consents != null) {
      for (GdprConsent c : this.consents) {
        c.setUser(null);
      }
      this.consents.clear();
    }
  }

  public KycVerification getCurrentKycVerification() {
    if (kycVerifications == null || kycVerifications.isEmpty()) {
      return null;
    }
    return kycVerifications.getFirst(); // la plus récente grâce à @OrderBy
  }

  public void addKycVerification(KycVerification kycVerification) {
    if (this.kycVerifications == null) {
      this.kycVerifications = new ArrayList<>();
    }
    this.kycVerifications.addFirst(kycVerification); // ajoute en première position
    kycVerification.setUser(this);
  }
}
