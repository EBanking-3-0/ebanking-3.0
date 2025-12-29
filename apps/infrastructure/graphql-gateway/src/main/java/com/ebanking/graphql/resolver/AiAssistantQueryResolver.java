package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.AiAssistantServiceClient;
import com.ebanking.graphql.model.ConversationDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AiAssistantQueryResolver {

  private final AiAssistantServiceClient aiAssistantServiceClient;

  public AiAssistantQueryResolver(AiAssistantServiceClient aiAssistantServiceClient) {
    this.aiAssistantServiceClient = aiAssistantServiceClient;
  }

  @QueryMapping
  public ConversationDTO conversation(@Argument String id) {
    return aiAssistantServiceClient.getConversation(id);
  }
}
