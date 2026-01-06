package com.ebanking.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai.assistant.conversation")
public class ConversationProperties {

  /** Maximum age of a conversation in days before it is expired. 0 disables TTL enforcement. */
  private int ttlDays = 90;

  /** Maximum number of messages retained per conversation. 0 disables message pruning. */
  private int maxMessages = 100;
}
