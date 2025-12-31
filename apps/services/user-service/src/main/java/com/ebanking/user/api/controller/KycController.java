package com.ebanking.user.api.controller;

import com.ebanking.shared.dto.KycRequest;
import com.ebanking.shared.dto.KycResponse;
import com.ebanking.user.api.mapper.KycMapper;
import com.ebanking.user.application.service.UserService;
import com.ebanking.user.domain.model.KycVerification;
import com.ebanking.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final UserService userService;
    private final KycMapper kycMapper;

    /**
     * Soumettre une vérification KYC avec upload de fichiers
     *
     * Le corps de la requête est multipart/form-data :
     * - "data" : JSON contenant les informations textuelles (KycRequest)
     * - "cinImage" : fichier image de la CIN
     * - "selfieImage" : fichier selfie
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<KycResponse> submitKyc(
            @Valid @RequestPart("data") KycRequest kycRequest,
            @RequestPart(value = "cinImage", required = false) MultipartFile cinImage,
            @RequestPart(value = "selfieImage", required = false) MultipartFile selfieImage,
            Authentication authentication) {

        try {
            KycVerification kycVerification = userService.submitKycWithUserCreation(
                    authentication, kycRequest, cinImage, selfieImage);

            KycResponse response = kycMapper.toResponse(kycVerification);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            // En prod, utilise un logger au lieu de printStackTrace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer le statut KYC de l'utilisateur actuel
     */
    @GetMapping("/status")
    public ResponseEntity<KycResponse> getKycStatus(Authentication authentication) {
        String keycloakId = userService.getKeycloakIdFromJwt(authentication);

        User user = userService.getUserByKeycloakIdOptional(keycloakId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        KycVerification kycVerification = userService.getKycVerification(user);
        if (kycVerification == null) {
            return ResponseEntity.notFound().build();
        }

        KycResponse response = kycMapper.toResponse(kycVerification);
        return ResponseEntity.ok(response);
    }
}