package com.ebanking.user.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * KycVerification - Entité de domaine représentant la vérification Know Your Customer (KYC)
 *
 * La vérification KYC est le processus d'identification et de vérification des utilisateurs.
 * Elle capture:
 * - Les informations d'identité (numéro CIN)
 * - Les documents d'identité (photo d'ID)
 * - La photo de l'utilisateur (selfie)
 * - Le statut de vérification (en attente, approuvé, rejeté)
 *
 * Statuts possibles:
 * - PENDING_REVIEW: Soumise et en attente de révision manuelle
 * - VERIFIED: KYC approuvée, utilisateur peut accéder au service
 * - REJECTED: KYC rejetée, utilisateur doit resoumette ou contacter le support
 * - MORE_INFO_NEEDED: Informations supplémentaires requises
 *
 * Relation:
 * - OneToOne avec User: Un utilisateur a une vérification KYC
 */
@Entity
@Table(name = "kyc_verifications", schema = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycVerification {

    /**
     * UUID unique généré automatiquement
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Relation OneToOne vers l'utilisateur
     * Chaque vérification KYC est liée à exactement un utilisateur
     * - JoinColumn: Définit la clé étrangère "user_id"
     * - Unique: Un utilisateur ne peut avoir qu'une seule vérification KYC
     * - Nullable: Une vérification KYC doit toujours être liée à un utilisateur
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Numéro d'identification (CIN - Carte d'Identité Nationale)
     * Identifiant unique du document d'identité physique
     * Obligatoire et doit être valide
     */
    @Column(nullable = false)
    private String cinNumber;

    /**
     * URL du document d'identité stocké
     * Contient le chemin ou l'URL du document d'identité numérisé (photo de CIN)
     * Obligatoire pour la vérification
     */
    private String idDocumentUrl;

    /**
     * URL de la preuve d'adresse stockée
     * Contient le chemin ou l'URL d'un document prouvant l'adresse (facture, etc.)
     * Optionnel selon les exigences de conformité
     */
    private String addressProofUrl;

    /**
     * URL de la photo de l'utilisateur (selfie)
     * Contient le chemin ou l'URL du selfie de l'utilisateur
     * Utilisé pour vérifier que le document appartient à l'utilisateur
     * Obligatoire pour la vérification biométrique
     */
    private String selfieUrl;

    /**
     * Statut actuel de la vérification KYC
     * PENDING_REVIEW: En attente de révision manuelle
     * VERIFIED: KYC approuvée
     * REJECTED: KYC rejetée
     * MORE_INFO_NEEDED: Information supplémentaire requise
     *
     * Défini à PENDING_REVIEW lors de la soumission
     * Mis à jour par un processus d'approbation externe
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING_REVIEW;

    /**
     * Timestamp de vérification
     * Enregistré quando la vérification est approuvée (status = VERIFIED)
     * Null jusqu'à l'approbation
     */
    private LocalDateTime verifiedAt;

    /**
     * Identifiant de l'agent qui a approuvé/rejeté la vérification
     * Peut être un utilisateur administrateur, un système automatisé, etc.
     * Null jusqu'à la décision
     */
    private String verifiedBy;

    /**
     * Timestamp de création
     * Défini automatiquement à la création de l'enregistrement
     * Immuable - enregistre quand la vérification a été soumise
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp de dernière mise à jour
     * Mis à jour automatiquement lors de chaque modification
     * Utile pour tracker les changements de statut
     */
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Énumération des statuts possibles d'une vérification KYC
     */
    public enum KycStatus {
        /**
         * Vérification soumise et en attente de révision manuelle
         */
        PENDING_REVIEW,

        /**
         * KYC approuvée et vérifiée
         */
        VERIFIED,

        /**
         * KYC rejetée - l'utilisateur ne peut pas utiliser le service
         */
        REJECTED,

        /**
         * Informations supplémentaires requises de la part de l'utilisateur
         */
        MORE_INFO_NEEDED
    }
}