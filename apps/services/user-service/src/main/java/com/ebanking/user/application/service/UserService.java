package com.ebanking.user.application.service;

import com.ebanking.shared.dto.KycRequest;
import com.ebanking.user.domain.model.*;
import com.ebanking.user.domain.model.GdprConsent.ConsentType;
import com.ebanking.user.domain.repository.GdprConsentRepository;
import com.ebanking.user.domain.repository.KycVerificationRepository;
import com.ebanking.user.domain.repository.UserRepository;
import com.ebanking.user.infrastructure.storage.FileStorageService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
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

  public User getUserById(String id) {
    try {
      UUID uuid = UUID.fromString(id);
      return userRepository.findById(uuid).orElse(null);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  // ==================== KYC SUBMISSION (version multipart) ====================

  @Transactional
  public KycVerification submitKycWithUserCreation(
      Authentication authentication,
      KycRequest kycRequest,
      MultipartFile cinImage,
      MultipartFile selfieImage)
      throws Exception {

    log.info("Starting KYC submission process...");
    String keycloakId = getKeycloakIdFromJwt(authentication);
    log.info("Extracted keycloakId: {}", keycloakId);

    // Récupère ou crée l'utilisateur
    User user =
        userRepository
            .findByKeycloakId(keycloakId)
            .orElseGet(
                () -> {
                  log.info("Creating new user for keycloakId: {}", keycloakId);
                  return User.builder()
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
                      .build();
                });

    // Vérifie qu'une KYC n'est pas déjà en attente
    if (isKycAlreadySubmitted(user)) {
      log.warn("KYC already submitted for user: {}", user.getId());
      throw new IllegalStateException("KYC verification is already submitted and pending review");
    }

    // Mise à jour du profil
    log.info("Updating user profile information...");
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
    log.info("User profile saved with ID: {}", user.getId());

    // Stockage des images
    String cinImageUrl = null;
    if (cinImage != null && !cinImage.isEmpty()) {
      log.info("Storing CIN image...");
      cinImageUrl = fileStorageService.storeFile(cinImage, user.getId().toString(), "cin");
      log.info("CIN image stored: {}", cinImageUrl);
    }

    String selfieUrl = null;
    if (selfieImage != null && !selfieImage.isEmpty()) {
      log.info("Storing selfie image...");
      selfieUrl = fileStorageService.storeFile(selfieImage, user.getId().toString(), "selfie");
      log.info("Selfie image stored: {}", selfieUrl);
    }

    // Création KYC
    log.info("Creating KycVerification record...");
    KycVerification kycVerification =
        KycVerification.builder()
            .user(user)
            .cinNumber(kycRequest.getCinNumber())
            .idDocumentUrl(cinImageUrl)
            .selfieUrl(selfieUrl)
            .status(KycVerification.KycStatus.PENDING_REVIEW)
            .build();

    kycVerification = kycVerificationRepository.save(kycVerification);
    log.info("KycVerification record saved: {}", kycVerification.getId());

    // Consentements GDPR
    if (kycRequest.getGdprConsents() != null && !kycRequest.getGdprConsents().isEmpty()) {
      log.info("Processing GDPR consents...");
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
          log.warn("Invalid consent type ignored: {}", entry.getKey());
        }
      }
      log.info("GDPR consents processed.");
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
