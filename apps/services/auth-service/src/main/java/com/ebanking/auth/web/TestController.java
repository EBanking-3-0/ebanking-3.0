package com.ebanking.auth.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/hello")
  @PreAuthorize("isAuthenticated()")
  public String hello() {
    return "Hello from Auth Service!";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('admin')")
  public String adminHello() {
    return "Hello Admin from Auth Service!";
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('user')")
  public String userHello() {
    return "Hello User from Auth Service!";
  }
}
