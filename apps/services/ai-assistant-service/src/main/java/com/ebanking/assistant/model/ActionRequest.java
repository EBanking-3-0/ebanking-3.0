package com.ebanking.assistant.model;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {

  @NotBlank(message = "Action name cannot be blank")
  private String actionName;

  @NotBlank(message = "UserId cannot be blank")
  private Long userId;

  private Map<String, Object> parameters = new HashMap<>();
}
