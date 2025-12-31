package com.ebanking.assistant.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ebanking.assistant.config.ConversationProperties;
import com.ebanking.assistant.model.Conversation;
import com.ebanking.assistant.model.Message;
import com.ebanking.assistant.repository.ConversationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

  @Mock private ConversationRepository conversationRepository;
  private ConversationService conversationService;
  private ConversationProperties conversationProperties;

  @BeforeEach
  void setUp() {
    conversationProperties = new ConversationProperties();
    conversationProperties.setTtlDays(30);
    conversationProperties.setMaxMessages(100);
    conversationService = new ConversationService(conversationRepository, conversationProperties);
  }

  @Test
  void testCreateConversation() {
    Conversation savedConversation =
        Conversation.builder()
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
    Conversation existing =
        Conversation.builder()
            .id("test-id")
            .userId(1L)
            .sessionId("session-123")
            .messages(new ArrayList<>())
            .build();

    when(conversationRepository.findById("test-id")).thenReturn(Optional.of(existing));
    when(conversationRepository.save(any(Conversation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Message message = Message.builder().role(Message.Role.USER).content("Hello").build();

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

  @Test
  void testPrunesMessagesWhenOverLimit() {
    conversationProperties.setMaxMessages(2);

    List<Message> messages = new ArrayList<>();
    messages.add(Message.builder().content("m1").build());
    messages.add(Message.builder().content("m2").build());
    messages.add(Message.builder().content("m3").build());

    Conversation existing =
        Conversation.builder()
            .id("test-id")
            .userId(1L)
            .sessionId("session-123")
            .messages(messages)
            .updatedAt(LocalDateTime.now())
            .build();

    when(conversationRepository.findById("test-id")).thenReturn(Optional.of(existing));
    when(conversationRepository.save(any(Conversation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Message message = Message.builder().role(Message.Role.USER).content("Hello").build();

    Conversation updated = conversationService.addMessage("test-id", message);

    assertEquals(2, updated.getMessages().size());
    assertEquals("m3", updated.getMessages().get(0).getContent());
    assertEquals("Hello", updated.getMessages().get(1).getContent());
  }

  @Test
  void testExpiresConversationByTtl() {
    conversationProperties.setTtlDays(1);

    Conversation expired =
        Conversation.builder()
            .id("old")
            .userId(1L)
            .sessionId("session-1")
            .updatedAt(LocalDateTime.now().minusDays(5))
            .build();

    when(conversationRepository.findById("old")).thenReturn(Optional.of(expired));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                conversationService.addMessage(
                    "old", Message.builder().content("hi").role(Message.Role.USER).build()));

    assertTrue(ex.getMessage().contains("expired"));
    verify(conversationRepository).deleteById("old");
  }
}
