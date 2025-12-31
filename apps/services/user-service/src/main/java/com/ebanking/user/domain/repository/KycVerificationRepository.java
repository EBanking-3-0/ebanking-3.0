package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KycVerificationRepository  extends JpaRepository<KycVerification, UUID> {
}
