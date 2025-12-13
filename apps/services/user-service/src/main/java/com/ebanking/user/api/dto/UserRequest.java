/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ebanking.user.api.dto;

import com.ebanking.user.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phone;

    private boolean rgpdConsent;

    // Mapping method to convert DTO to Domain Entity
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phone(this.phone)
                .rgpdConsent(this.rgpdConsent)
                .build();
    }
}
