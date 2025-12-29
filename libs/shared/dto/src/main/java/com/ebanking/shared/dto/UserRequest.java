package com.ebanking.shared.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private boolean rgpdConsent = false;
}


