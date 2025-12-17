package com.ebanking.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponseDTO {
    private boolean success;
    private Object result;
    private String error;
}
