package com.ebanking.assistant.repository;

import com.ebanking.assistant.model.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

  List<Conversation> findByUserId(String userId);

  Optional<Conversation> findBySessionId(String sessionId);

  List<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId);
}
