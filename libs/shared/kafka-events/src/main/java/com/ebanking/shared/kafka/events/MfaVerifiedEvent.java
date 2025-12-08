package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Event published when MFA verification is completed.
 * Published by: Auth Service
 * Consumed by: Audit Service
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MfaVerifiedEvent extends BaseEvent {
    
    private Long userId;
    private String email;
    private String mfaType; // SMS, EMAIL, BIOMETRIC, TOTP
    private boolean verified;
    private String ipAddress;
    
    public MfaVerifiedEvent() {
        super(KafkaTopics.MFA_VERIFIED);
    }
    
    public MfaVerifiedEvent(Long userId, String email, String mfaType, 
                            boolean verified, String ipAddress) {
        super(KafkaTopics.MFA_VERIFIED);
        this.userId = userId;
        this.email = email;
        this.mfaType = mfaType;
        this.verified = verified;
        this.ipAddress = ipAddress;
    }
}

