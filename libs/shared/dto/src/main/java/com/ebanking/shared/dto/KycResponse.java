package com.ebanking.shared.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KycResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String cinNumber;
    private String idDocumentUrl;
    private String selfieUrl;
    private String status;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

