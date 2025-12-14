package com.ebanking.assistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String response;
    
    private String conversationId;
    
    private String sessionId;
    
    private String intent;
    
    private String actionExecuted;
    
    @Builder.Default
    private Map<String, Object> actionResult = new HashMap<>();
}
