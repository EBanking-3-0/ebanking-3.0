package com.ebanking.assistant.service;

import com.ebanking.assistant.model.Conversation;
import com.ebanking.assistant.model.Message;
import com.ebanking.assistant.repository.ConversationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    
    public Conversation createConversation(Long userId, String sessionId) {
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .sessionId(sessionId != null ? sessionId : UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Conversation saved = conversationRepository.save(conversation);
        log.info("Created conversation {} for user {}", saved.getId(), userId);
        return saved;
    }
    
    public Conversation getOrCreateConversation(Long userId, String conversationId, String sessionId) {
        if (conversationId != null && !conversationId.isEmpty()) {
            Optional<Conversation> existing = conversationRepository.findById(conversationId);
            if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
                return existing.get();
            }
        }
        
        if (sessionId != null && !sessionId.isEmpty()) {
            Optional<Conversation> existing = conversationRepository.findBySessionId(sessionId);
            if (existing.isPresent() && existing.get().getUserId().equals(userId)) {
                return existing.get();
            }
        }
        
        return createConversation(userId, sessionId);
    }
    
    public Conversation addMessage(String conversationId, Message message) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));
        
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        conversation.addMessage(message);
        return conversationRepository.save(conversation);
    }
    
    public Optional<Conversation> getConversation(String conversationId) {
        return conversationRepository.findById(conversationId);
    }
    
    public List<Conversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }
    
    public void deleteConversation(String conversationId) {
        conversationRepository.deleteById(conversationId);
        log.info("Deleted conversation {}", conversationId);
    }
}
