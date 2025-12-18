package com.ebanking.graphql.client;

import com.ebanking.graphql.model.UserDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

  @GetMapping("/api/users")
  List<UserDTO> getAllUsers();

  @GetMapping("/api/users/{id}")
  UserDTO getUserById(@PathVariable("id") Long id);

  @GetMapping("/api/users/email/{email}")
  UserDTO getUserByEmail(@PathVariable("email") String email);

  @PostMapping("/api/users")
  UserDTO createUser(@RequestBody UserDTO userDTO);

  @PutMapping("/api/users/{id}")
  UserDTO updateUser(@PathVariable("id") Long id, @RequestBody UserDTO userDTO);

  @DeleteMapping("/api/users/{id}")
  void deleteUser(@PathVariable("id") Long id);
}
