package com.ebanking.shared.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KycRequest implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotBlank(message = "Phone number is required")
  private String phone;

  private String addressLine1;
  private String addressLine2;
  private String city;
  private String postalCode;
  private String country;

  @NotBlank(message = "CIN number is required")
  private String cinNumber;

  // File uploads will be handled separately via multipart/form-data
  // These will be base64 encoded or file paths
  private String cinImageBase64; // CIN document image
  private String selfieImageBase64; // Selfie image

  // GDPR Consents - Map of consent type to granted (true/false)
  private Map<String, Boolean> gdprConsents;
}
