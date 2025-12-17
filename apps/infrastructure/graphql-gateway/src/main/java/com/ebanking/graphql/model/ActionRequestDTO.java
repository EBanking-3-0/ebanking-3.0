package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequestDTO {
    private String actionName;
    private Map<String, Object> parameters;
}
