package com.ebanking.assistant.service;

import com.ebanking.assistant.model.Conversation;
import com.ebanking.assistant.model.Message;
import com.ebanking.assistant.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
    
    @Mock
    private ConversationRepository conversationRepository;
    
    @InjectMocks
    private ConversationService conversationService;
    
    @BeforeEach
    void setUp() {
        // Setup mock behavior
    }
    
    @Test
    void testCreateConversation() {
        Conversation savedConversation = Conversation.builder()
                .id("test-id")
                .userId(1L)
                .sessionId("session-123")
                .createdAt(LocalDateTime.now())
                .build();
        
        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
        
        Conversation conversation = conversationService.createConversation(1L, "session-123");
        
        assertNotNull(conversation.getId());
        assertEquals(1L, conversation.getUserId());
        assertEquals("session-123", conversation.getSessionId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }
    
    @Test
    void testAddMessage() {
        Conversation existing = Conversation.builder()
                .id("test-id")
                .userId(1L)
                .sessionId("session-123")
                .messages(new ArrayList<>())
                .build();
        
        when(conversationRepository.findById("test-id")).thenReturn(Optional.of(existing));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Message message = Message.builder()
                .role(Message.Role.USER)
                .content("Hello")
                .build();
        
        Conversation updated = conversationService.addMessage("test-id", message);
        
        assertEquals(1, updated.getMessages().size());
        assertEquals("Hello", updated.getMessages().get(0).getContent());
    }
    
    @Test
    void testGetUserConversations() {
        List<Conversation> conversations = new ArrayList<>();
        conversations.add(Conversation.builder().id("1").userId(1L).build());
        conversations.add(Conversation.builder().id("2").userId(1L).build());
        
        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(1L)).thenReturn(conversations);
        
        List<Conversation> userConversations = conversationService.getUserConversations(1L);
        
        assertEquals(2, userConversations.size());
    }
}
