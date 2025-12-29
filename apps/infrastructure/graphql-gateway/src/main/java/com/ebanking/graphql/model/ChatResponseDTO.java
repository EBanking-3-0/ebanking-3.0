package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
  private String conversationId;
  private String sessionId;
  private String response;
  private String intent;
  private String actionExecuted;
  private Object actionResult;
}
