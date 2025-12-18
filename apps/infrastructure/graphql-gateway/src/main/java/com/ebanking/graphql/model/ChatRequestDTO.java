package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
  private String message;
  private String conversationId;
  private String sessionId;
}
