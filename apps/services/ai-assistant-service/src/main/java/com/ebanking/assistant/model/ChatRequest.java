package com.ebanking.assistant.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be blank")
    private String message;
    
    private String conversationId;
    
    private String sessionId;
}
