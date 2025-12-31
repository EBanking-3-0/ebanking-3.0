package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.GdprConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GdprConsentRepository extends JpaRepository<GdprConsent, UUID> {
}
