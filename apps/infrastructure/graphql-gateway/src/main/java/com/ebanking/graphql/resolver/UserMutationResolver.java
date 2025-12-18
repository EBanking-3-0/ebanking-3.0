package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.UserServiceClient;
import com.ebanking.graphql.model.UserDTO;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserMutationResolver {

  private final UserServiceClient userServiceClient;

  @MutationMapping
  public UserDTO createUser(@Argument Map<String, Object> input) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail((String) input.get("email"));
    userDTO.setFirstName((String) input.get("firstName"));
    userDTO.setLastName((String) input.get("lastName"));
    userDTO.setPhone((String) input.get("phone"));
    return userServiceClient.createUser(userDTO);
  }

  @MutationMapping
  public UserDTO updateUser(@Argument String id, @Argument Map<String, Object> input) {
    try {
      Long userId = Long.parseLong(id);
      UserDTO userDTO = new UserDTO();
      userDTO.setFirstName((String) input.get("firstName"));
      userDTO.setLastName((String) input.get("lastName"));
      userDTO.setPhone((String) input.get("phone"));
      return userServiceClient.updateUser(userId, userDTO);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid user ID format: " + id);
    }
  }

  @MutationMapping
  public Boolean deleteUser(@Argument String id) {
    try {
      Long userId = Long.parseLong(id);
      userServiceClient.deleteUser(userId);
      return true;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid user ID format: " + id);
    }
  }
}
