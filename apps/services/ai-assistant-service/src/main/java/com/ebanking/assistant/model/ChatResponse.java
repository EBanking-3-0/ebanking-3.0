package com.ebanking.assistant.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

  private String response;

  private String conversationId;

  private String sessionId;

  private String intent;

  private String actionExecuted;

  private Map<String, Object> actionResult;
}
