package com.ebanking.user.service;

import com.ebanking.shared.dto.UserDTO;
import com.ebanking.shared.kafka.events.UserCreatedEvent;
import com.ebanking.shared.kafka.events.UserUpdatedEvent;
import com.ebanking.shared.kafka.producer.TypedEventProducer;
import com.ebanking.user.model.User;
import com.ebanking.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final TypedEventProducer eventProducer;

  public List<UserDTO> getAllUsers() {
    return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  public UserDTO getUserById(Long id) {
    return userRepository
        .findById(id)
        .map(this::mapToDTO)
        .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
  }

  public UserDTO getUserByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .map(this::mapToDTO)
        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
  }

  @Transactional
  public UserDTO createUser(UserDTO userDTO) {
    User user = new User();
    user.setEmail(userDTO.getEmail());
    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setPhone(userDTO.getPhone());
    user.setStatus(User.UserStatus.ACTIVE);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());

    User savedUser = userRepository.save(user);

    // Publish user created event
    UserCreatedEvent event =
        UserCreatedEvent.builder()
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .username(savedUser.getEmail()) // Using email as username for now
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .status(savedUser.getStatus().toString())
            .source("user-service")
            .build();

    eventProducer.publishUserCreated(event);
    log.info("Published user.created event for user: {}", savedUser.getId());

    return mapToDTO(savedUser);
  }

  @Transactional
  public UserDTO updateUser(Long id, UserDTO userDTO) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    String oldFirstName = user.getFirstName();
    String oldLastName = user.getLastName();
    String oldPhone = user.getPhone();
    String oldStatus = user.getStatus().toString();

    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setPhone(userDTO.getPhone());
    user.setUpdatedAt(LocalDateTime.now());

    User updatedUser = userRepository.save(user);

    // Build updated fields string
    StringBuilder updatedFields = new StringBuilder();
    if (!oldFirstName.equals(updatedUser.getFirstName())) {
      updatedFields.append("firstName,");
    }
    if (!oldLastName.equals(updatedUser.getLastName())) {
      updatedFields.append("lastName,");
    }
    if (!oldPhone.equals(updatedUser.getPhone())) {
      updatedFields.append("phone,");
    }

    // Publish user updated event
    UserUpdatedEvent event =
        UserUpdatedEvent.builder()
            .userId(updatedUser.getId())
            .email(updatedUser.getEmail())
            .firstName(updatedUser.getFirstName())
            .lastName(updatedUser.getLastName())
            .status(updatedUser.getStatus().toString())
            .updatedFields(
                updatedFields.length() > 0
                    ? updatedFields.substring(0, updatedFields.length() - 1)
                    : "")
            .source("user-service")
            .build();

    eventProducer.publishUserUpdated(event);
    log.info("Published user.updated event for user: {}", updatedUser.getId());

    return mapToDTO(updatedUser);
  }

  @Transactional
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }

  private UserDTO mapToDTO(User user) {
    return new UserDTO(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhone(),
        user.getStatus().name());
  }
}
