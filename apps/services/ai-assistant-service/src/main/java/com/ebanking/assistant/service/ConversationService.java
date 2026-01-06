package com.ebanking.assistant.service;

import com.ebanking.assistant.config.ConversationProperties;
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
  private final ConversationProperties conversationProperties;

  public Conversation createConversation(Long userId, String sessionId) {
    Conversation conversation =
        Conversation.builder()
            .userId(userId)
            .sessionId(sessionId != null ? sessionId : UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    Conversation saved = conversationRepository.save(conversation);
    log.info("Created conversation {} for user {}", saved.getId(), userId);
    return saved;
  }

  public Conversation getOrCreateConversation(
      Long userId, String conversationId, String sessionId) {
    if (conversationId != null && !conversationId.isEmpty()) {
      Optional<Conversation> existing = conversationRepository.findById(conversationId);
      Optional<Conversation> processed = processFoundForUser(existing, userId);
      if (processed.isPresent()) {
        return processed.get();
      }
    }

    if (sessionId != null && !sessionId.isEmpty()) {
      Optional<Conversation> existing = conversationRepository.findBySessionId(sessionId);
      Optional<Conversation> processed = processFoundForUser(existing, userId);
      if (processed.isPresent()) {
        return processed.get();
      }
    }

    return createConversation(userId, sessionId);
  }

  public Conversation addMessage(String conversationId, Message message) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

    Optional<Conversation> refreshed = refreshConversation(conversation);
    if (refreshed.isEmpty()) {
      throw new RuntimeException("Conversation expired and was removed. Please start a new chat.");
    }

    conversation = refreshed.get();

    if (message.getTimestamp() == null) {
      message.setTimestamp(LocalDateTime.now());
    }

    conversation.addMessage(message);
    boolean pruned = applyMessageLimit(conversation);
    if (pruned) {
      log.debug(
          "Pruned conversation {} to max {} messages",
          conversationId,
          conversationProperties.getMaxMessages());
    }
    return conversationRepository.save(conversation);
  }

  public Optional<Conversation> getConversation(String conversationId) {
    Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
    if (conversationOpt.isEmpty()) {
      return Optional.empty();
    }

    return refreshConversation(conversationOpt.get());
  }

  public List<Conversation> getUserConversations(Long userId) {
    List<Conversation> conversations =
        conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    List<Conversation> result = new java.util.ArrayList<>();
    for (Conversation c : conversations) {
      Optional<Conversation> refreshed = refreshConversation(c);
      refreshed.ifPresent(result::add);
    }
    return result;
  }

  public void deleteConversation(String conversationId) {
    conversationRepository.deleteById(conversationId);
    log.info("Deleted conversation {}", conversationId);
  }

  private boolean isExpired(Conversation conversation) {
    int ttlDays = conversationProperties.getTtlDays();
    if (ttlDays <= 0) {
      return false;
    }

    LocalDateTime cutoff = LocalDateTime.now().minusDays(ttlDays);
    LocalDateTime referenceTime =
        conversation.getUpdatedAt() != null
            ? conversation.getUpdatedAt()
            : conversation.getCreatedAt();

    return referenceTime != null && referenceTime.isBefore(cutoff);
  }

  private boolean applyMessageLimit(Conversation conversation) {
    int maxMessages = conversationProperties.getMaxMessages();
    if (maxMessages <= 0 || conversation.getMessages() == null) {
      return false;
    }

    int size = conversation.getMessages().size();
    if (size <= maxMessages) {
      return false;
    }

    int fromIndex = size - maxMessages;
    conversation.setMessages(
        new java.util.ArrayList<>(conversation.getMessages().subList(fromIndex, size)));
    conversation.setUpdatedAt(LocalDateTime.now());
    return true;
  }

  private Optional<Conversation> processFoundForUser(Optional<Conversation> existing, Long userId) {
    if (existing.isEmpty()) {
      return Optional.empty();
    }
    Conversation conversation = existing.get();
    if (!conversation.getUserId().equals(userId)) {
      return Optional.empty();
    }
    return refreshConversation(conversation);
  }

  private Optional<Conversation> refreshConversation(Conversation conversation) {
    if (isExpired(conversation)) {
      conversationRepository.deleteById(conversation.getId());
      log.info("Conversation {} expired and was deleted", conversation.getId());
      return Optional.empty();
    }

    boolean pruned = applyMessageLimit(conversation);
    if (pruned) {
      conversation = conversationRepository.save(conversation);
    }
    return Optional.of(conversation);
  }
}
