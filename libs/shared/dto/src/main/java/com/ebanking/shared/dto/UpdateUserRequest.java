package com.ebanking.shared.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
  private String firstName;
  private String lastName;
  private String phone;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String postalCode;
  private String country;
  private Boolean consents;
}
