package com.ebanking.shared.kafka.events;

import com.ebanking.shared.kafka.KafkaTopics;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;           
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Event published when a new user is created/registered.
 */
@Data
@Builder                          
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent extends BaseEvent {

    private Long userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String status;

}