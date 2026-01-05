package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.GdprConsent;
import com.ebanking.user.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GdprConsentRepository extends JpaRepository<GdprConsent, UUID> {
  Optional<GdprConsent> findByUserAndConsentTypeAndGranted(User user, GdprConsent.ConsentType consentType, boolean granted);
}
