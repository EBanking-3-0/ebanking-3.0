package com.ebanking.assistant.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

  @GetMapping("/api/users/{id}")
  Map<String, Object> getUserById(@PathVariable("id") Long id);

  @GetMapping("/api/users/email/{email}")
  Map<String, Object> getUserByEmail(@PathVariable("email") String email);
}
