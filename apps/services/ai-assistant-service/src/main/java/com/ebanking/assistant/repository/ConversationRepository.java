package com.ebanking.assistant.repository;

import com.ebanking.assistant.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    
    List<Conversation> findByUserId(Long userId);
    
    Optional<Conversation> findBySessionId(String sessionId);
    
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
