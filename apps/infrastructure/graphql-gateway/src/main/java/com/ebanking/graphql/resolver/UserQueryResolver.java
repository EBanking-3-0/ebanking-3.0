package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.UserServiceClient;
import com.ebanking.graphql.model.UserDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserQueryResolver {

  private final UserServiceClient userServiceClient;

  @QueryMapping
  public List<UserDTO> users() {
    return userServiceClient.getAllUsers();
  }

  @QueryMapping
  public UserDTO user(@Argument String id) {
    try {
      Long userId = Long.parseLong(id);
      return userServiceClient.getUserById(userId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid user ID format: " + id);
    }
  }

  @QueryMapping
  public UserDTO userByEmail(@Argument String email) {
    return userServiceClient.getUserByEmail(email);
  }
}
