package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.GdprConsent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GdprConsentRepository extends JpaRepository<GdprConsent, UUID> {}
