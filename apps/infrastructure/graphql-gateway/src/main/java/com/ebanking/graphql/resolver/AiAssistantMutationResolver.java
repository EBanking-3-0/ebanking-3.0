package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.AiAssistantServiceClient;
import com.ebanking.graphql.model.ActionRequestDTO;
import com.ebanking.graphql.model.ActionResponseDTO;
import com.ebanking.graphql.model.ChatRequestDTO;
import com.ebanking.graphql.model.ChatResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AiAssistantMutationResolver {

  private final AiAssistantServiceClient aiAssistantServiceClient;
  private final ObjectMapper objectMapper;

  public AiAssistantMutationResolver(
      AiAssistantServiceClient aiAssistantServiceClient, ObjectMapper objectMapper) {
    this.aiAssistantServiceClient = aiAssistantServiceClient;
    this.objectMapper = objectMapper;
  }

  @MutationMapping
  public ChatResponseDTO sendChatMessage(@Argument Map<String, Object> input) {
    ChatRequestDTO chatRequest = new ChatRequestDTO();
    chatRequest.setMessage((String) input.get("message"));
    chatRequest.setConversationId((String) input.get("conversationId"));
    chatRequest.setSessionId((String) input.get("sessionId"));

    return aiAssistantServiceClient.sendChatMessage(chatRequest);
  }

  @MutationMapping
  public ActionResponseDTO executeAction(@Argument Map<String, Object> input) {
    ActionRequestDTO actionRequest = new ActionRequestDTO();
    actionRequest.setActionName((String) input.get("actionName"));

    try {
      String parametersJson = (String) input.get("parameters");
      @SuppressWarnings("unchecked")
      Map<String, Object> parameters = objectMapper.readValue(parametersJson, Map.class);
      actionRequest.setParameters(parameters);
    } catch (Exception e) {
      throw new RuntimeException("Invalid parameters JSON: " + e.getMessage(), e);
    }

    return aiAssistantServiceClient.executeAction(actionRequest);
  }
}
