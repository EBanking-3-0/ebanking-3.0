package com.ebanking.graphql.client;

import com.ebanking.graphql.model.ChatRequestDTO;
import com.ebanking.graphql.model.ChatResponseDTO;
import com.ebanking.graphql.model.ActionRequestDTO;
import com.ebanking.graphql.model.ActionResponseDTO;
import com.ebanking.graphql.model.ConversationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ai-assistant-service")
public interface AiAssistantServiceClient {

    @PostMapping("/api/chat")
    ChatResponseDTO sendChatMessage(@RequestBody ChatRequestDTO chatRequest);

    @PostMapping("/api/actions/execute")
    ActionResponseDTO executeAction(@RequestBody ActionRequestDTO actionRequest);

    @GetMapping("/api/conversations/{id}")
    ConversationDTO getConversation(@PathVariable("id") String id);
}
