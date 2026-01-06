package com.ebanking.graphql.model;

import lombok.Data;

@Data
public class CreateAccountInput {
  private Long userId;
  private String type;
  private String currency;
  private String nickname;
}
