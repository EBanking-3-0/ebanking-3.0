package com.ebanking.auth.service;

import com.ebanking.shared.kafka.events.AuthLoginEvent;
import com.ebanking.shared.kafka.events.MfaVerifiedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Auth service with Kafka event publishing.
 * Publishes auth.login and mfa.verified events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TypedEventProducer eventProducer;

    public void handleLogin(Long userId, String email, String ipAddress, String userAgent, String loginMethod, boolean success) {
        // Login logic would go here
        log.info("User login: {} - Method: {} - Success: {}", email, loginMethod, success);
        
        // Publish login event
        AuthLoginEvent event = AuthLoginEvent.builder()
            .userId(userId)
            .email(email)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .loginMethod(loginMethod)
            .success(success)
            .source("auth-service")
            .build();
        
        eventProducer.publishAuthLogin(event);
        log.info("Published auth.login event for user: {}", userId);
    }

    public void handleMfaVerification(Long userId, String email, String mfaType, boolean verified, String ipAddress) {
        // MFA verification logic would go here
        log.info("MFA verification for user: {} - Type: {} - Verified: {}", email, mfaType, verified);
        
        // Publish MFA verified event
        MfaVerifiedEvent event = MfaVerifiedEvent.builder()
            .userId(userId)
            .email(email)
            .mfaType(mfaType)
            .verified(verified)
            .ipAddress(ipAddress)
            .source("auth-service")
            .build();
        
        eventProducer.publishMfaVerified(event);
        log.info("Published mfa.verified event for user: {}", userId);
    }
}

