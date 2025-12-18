package com.ebanking.assistant.controller;

import com.ebanking.assistant.model.ChatRequest;
import com.ebanking.assistant.model.ChatResponse;
import com.ebanking.assistant.model.Conversation;
import com.ebanking.assistant.model.Message;
import com.ebanking.assistant.producer.AssistantEventProducer;
import com.ebanking.assistant.service.AiService;
import com.ebanking.assistant.service.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final AiService aiService;
    private final ConversationService conversationService;
    private final AssistantEventProducer eventProducer;
    
    @PostMapping
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ChatResponse.builder()
                            .response("Authentication required. Please provide a valid JWT token.")
                            .build());
        }
        
        try {
            // Get or create conversation
            boolean isNewConversation = request.getConversationId() == null || request.getConversationId().isEmpty();
            Conversation conversation = conversationService.getOrCreateConversation(
                    userId,
                    request.getConversationId(),
                    request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString()
            );
            
            // Publish conversation started event if new
            if (isNewConversation) {
                eventProducer.publishConversationStarted(userId, conversation.getId(), conversation.getSessionId());
            }
            
            // Publish message received event
            eventProducer.publishMessageReceived(userId, conversation.getId(), request.getMessage());
            
            // Save user message
            Message userMessage = Message.builder()
                    .role(Message.Role.USER)
                    .content(request.getMessage())
                    .build();
            conversation = conversationService.addMessage(conversation.getId(), userMessage);
            
            // Process message with AI
            String memoryId = conversation.getId();
            ChatResponse response = aiService.processMessage(
                    request.getMessage(),
                    userId,
                    memoryId
            );
            
            // Save AI response
            Message aiMessage = Message.builder()
                    .role(Message.Role.ASSISTANT)
                    .content(response.getResponse())
                    .intent(response.getIntent())
                    .actionExecuted(response.getActionExecuted())
                    .actionResult(response.getActionResult())
                    .build();
            conversationService.addMessage(conversation.getId(), aiMessage);
            
            // Set conversation ID in response
            response.setConversationId(conversation.getId());
            response.setSessionId(conversation.getSessionId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.builder()
                            .response("I apologize, but I encountered an error. Please try again.")
                            .build());
        }
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getUserConversations(Authentication authentication) {
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable String id,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Conversation> conversationOpt = conversationService.getConversation(id);
        if (conversationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Conversation conversation = conversationOpt.get();
        // Verify ownership
        if (!conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(conversation);
    }
    
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String id,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Verify ownership before deletion
        Optional<Conversation> conversationOpt = conversationService.getConversation(id);
        if (conversationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Conversation conversation = conversationOpt.get();
        if (!conversation.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Assistant Service is UP!");
    }
    
    /**
     * Extract user ID from JWT token
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return null;
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        // Try different claim names for userId
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim != null) {
            return Long.valueOf(userIdClaim.toString());
        }
        
        // Try sub claim as fallback
        String sub = jwt.getSubject();
        if (sub != null) {
            try {
                return Long.valueOf(sub);
            } catch (NumberFormatException e) {
                // If sub is a UUID, convert it to a Long using hashCode
                // This provides a stable numeric ID for the session
                log.info("Converting UUID sub to numeric userId: {}", sub);
                return (long) Math.abs(sub.hashCode());
            }
        }
        
        return null;
    }
}
