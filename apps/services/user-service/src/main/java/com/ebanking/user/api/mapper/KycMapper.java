package com.ebanking.user.api.mapper;

import com.ebanking.shared.dto.KycResponse;
import com.ebanking.user.domain.model.KycVerification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * KYC Mapper - Convertit les entités KYC de domaine vers les DTO de réponse
 *
 * Responsabilités:
 * - Convertir KycVerification (entité domaine) vers KycResponse (DTO API)
 * - Transformer l'énumération de statut KYC en String
 *
 * MapStruct génère l'implémentation automatiquement au moment de la compilation.
 */
@Mapper(componentModel = "spring")
public interface KycMapper {

    /**
     * Convertit une entité KycVerification en réponse API KycResponse
     *
     * Mappages:
     * - status: Convertir l'énumération KycStatus en String (PENDING_REVIEW, VERIFIED, REJECTED, etc.)
     * - Les autres champs sont mappés par matching de nom
     *
     * @param kycVerification Entité KycVerification du domaine
     * @return KycResponse DTO pour la réponse API
     */
    @Mapping(target = "status", expression = "java(kycVerification.getStatus().name())")
    KycResponse toResponse(KycVerification kycVerification);
}

