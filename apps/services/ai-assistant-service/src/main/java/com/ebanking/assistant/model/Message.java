package com.ebanking.assistant.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

  public enum Role {
    USER,
    ASSISTANT
  }

  private Role role;

  private String content;

  private LocalDateTime timestamp;

  private String intent;

  private String actionExecuted;

  @Builder.Default private Map<String, Object> actionResult = new HashMap<>();

  @Builder.Default private Map<String, Object> metadata = new HashMap<>();
}
