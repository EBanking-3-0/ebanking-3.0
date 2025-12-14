package com.ebanking.assistant.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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
