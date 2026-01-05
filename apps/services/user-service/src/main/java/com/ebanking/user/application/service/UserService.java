package com.ebanking.user.application.service;

import com.ebanking.shared.dto.KycRequest;
import com.ebanking.user.domain.model.*;
import com.ebanking.user.domain.model.GdprConsent.ConsentType;
import com.ebanking.user.domain.repository.GdprConsentRepository;
import com.ebanking.user.domain.repository.KycVerificationRepository;
import com.ebanking.user.domain.repository.UserRepository;
import com.ebanking.user.infrastructure.storage.FileStorageService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final KycVerificationRepository kycVerificationRepository;
  private final GdprConsentRepository gdprConsentRepository;
  private final FileStorageService fileStorageService;

  // ==================== JWT EXTRACTION ====================

  public String getKeycloakIdFromJwt(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getClaim("sub");
  }

  public String getEmailFromJwt(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getClaim("email");
  }

  public String getFirstNameFromJwt(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getClaim("given_name") != null
            ? jwt.getClaim("given_name")
            : jwt.getClaim("firstName");
  }

  public String getLastNameFromJwt(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return jwt.getClaim("family_name") != null
            ? jwt.getClaim("family_name")
            : jwt.getClaim("lastName");
  }

  // ==================== USER MANAGEMENT ====================

  @Transactional(readOnly = true)
  public User getUserByKeycloakIdOptional(String keycloakId) {
    return userRepository.findByKeycloakId(keycloakId).orElse(null);
  }

  // ==================== KYC SUBMISSION (version multipart) ====================

  @Transactional
  public KycVerification submitKycWithUserCreation(
          Authentication authentication,
          KycRequest kycRequest,
          MultipartFile cinImage,
          MultipartFile selfieImage)
          throws Exception {

    String keycloakId = getKeycloakIdFromJwt(authentication);

    // Récupère ou crée l'utilisateur
    User user =
            userRepository
                    .findByKeycloakId(keycloakId)
                    .orElseGet(
                            () ->
                                    User.builder()
                                            .keycloakId(keycloakId)
                                            .email(
                                                    getEmailFromJwt(authentication) != null
                                                            ? getEmailFromJwt(authentication)
                                                            : "")
                                            .firstName(
                                                    getFirstNameFromJwt(authentication) != null
                                                            ? getFirstNameFromJwt(authentication)
                                                            : "")
                                            .lastName(
                                                    getLastNameFromJwt(authentication) != null
                                                            ? getLastNameFromJwt(authentication)
                                                            : "")
                                            .phone("")
                                            .status(User.UserStatus.PENDING_REVIEW)
                                            .build());

    // Vérifie qu'une KYC n'est pas déjà en attente
    if (isKycAlreadySubmitted(user)) {
      throw new IllegalStateException("KYC verification is already submitted and pending review");
    }

    // Mise à jour du profil
    user.setFirstName(kycRequest.getFirstName());
    user.setLastName(kycRequest.getLastName());
    user.setPhone(kycRequest.getPhone());
    user.setAddressLine1(kycRequest.getAddressLine1());
    user.setAddressLine2(kycRequest.getAddressLine2());
    user.setCity(kycRequest.getCity());
    user.setPostalCode(kycRequest.getPostalCode());
    user.setCountry(kycRequest.getCountry());
    user.setUpdatedAt(LocalDateTime.now());
    user.setStatus(User.UserStatus.PENDING_REVIEW);

    user = userRepository.save(user); // ID garanti

    // Stockage des images
    String cinImageUrl = null;
    if (cinImage != null && !cinImage.isEmpty()) {
      cinImageUrl = fileStorageService.storeFile(cinImage, user.getId().toString(), "cin");
    }

    String selfieUrl = null;
    if (selfieImage != null && !selfieImage.isEmpty()) {
      selfieUrl = fileStorageService.storeFile(selfieImage, user.getId().toString(), "selfie");
    }

    // Création KYC
    KycVerification kycVerification =
            KycVerification.builder()
                    .user(user)
                    .cinNumber(kycRequest.getCinNumber())
                    .idDocumentUrl(cinImageUrl)
                    .selfieUrl(selfieUrl)
                    .status(KycVerification.KycStatus.PENDING_REVIEW)
                    .build();

    kycVerification = kycVerificationRepository.save(kycVerification);

    // Consentements GDPR
    if (kycRequest.getGdprConsents() != null && !kycRequest.getGdprConsents().isEmpty()) {
      LocalDateTime now = LocalDateTime.now();
      for (var entry : kycRequest.getGdprConsents().entrySet()) {
        try {
          ConsentType type = ConsentType.valueOf(entry.getKey().toUpperCase());
          if (Boolean.TRUE.equals(entry.getValue())) {
            GdprConsent consent =
                    GdprConsent.builder()
                            .user(user)
                            .consentType(type)
                            .granted(true)
                            .grantedAt(now)
                            .consentVersion("v1.0")
                            .build();
            gdprConsentRepository.save(consent);
          }
        } catch (IllegalArgumentException ignored) {
        }
      }
    }

    return kycVerification;
  }

  public boolean isKycAlreadySubmitted(User user) {
    KycVerification current = user.getCurrentKycVerification();
    return current != null && current.getStatus() == KycVerification.KycStatus.PENDING_REVIEW;
  }

  @Transactional(readOnly = true)
  public KycVerification getKycVerification(User user) {
    return user.getCurrentKycVerification();
  }
}