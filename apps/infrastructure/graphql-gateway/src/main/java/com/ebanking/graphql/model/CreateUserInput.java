package com.ebanking.graphql.model;

import lombok.Data;

@Data
public class CreateUserInput {
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
}
