package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String id;
    private String userId;
    private String sessionId;
    private List<MessageDTO> messages;
    private String createdAt;
    private String updatedAt;
}
