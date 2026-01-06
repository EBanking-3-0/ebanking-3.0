package com.ebanking.shared.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponse implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private String id;
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private String status;
  private String kycStatus;
  private LocalDateTime createdAt;
}
