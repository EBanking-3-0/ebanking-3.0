package com.ebanking.user.api.dto;

import com.ebanking.user.domain.model.User;
import com.ebanking.user.domain.model.User.KycStatus;
import com.ebanking.user.domain.model.User.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    private boolean rgpdConsent = false; // optional: default false

    public User toEntity() {
        return User.builder()
                .email(this.email)
                .username(this.username)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phone(this.phone)
                .rgpdConsent(this.rgpdConsent)
                .status(UserStatus.ACTIVE)          // ← explicitly set
                .kycStatus(KycStatus.PENDING)       // ← explicitly set (fixes the null!)
                .build();
    }
}