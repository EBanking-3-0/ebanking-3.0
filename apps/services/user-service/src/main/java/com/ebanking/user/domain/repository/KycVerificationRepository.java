package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.KycVerification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycVerificationRepository extends JpaRepository<KycVerification, UUID> {}
