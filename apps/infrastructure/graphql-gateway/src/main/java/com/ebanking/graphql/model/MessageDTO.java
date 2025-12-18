package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
  private String role;
  private String content;
  private String timestamp;
  private String intent;
  private String actionExecuted;
  private Object actionResult;
}
