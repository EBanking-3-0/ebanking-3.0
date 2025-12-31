package com.ebanking.assistant.controller;

import com.ebanking.assistant.model.ActionRequest;
import com.ebanking.assistant.service.ActionExecutorService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller for direct action execution (admin/testing purposes). In production, this should be
 * restricted to admin users only.
 */
@Slf4j
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActionController {

  private final ActionExecutorService actionExecutorService;

  @PostMapping("/execute")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> executeAction(
      @Valid @RequestBody ActionRequest request, Authentication authentication) {

    Long userId = extractUserId(authentication);

    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authenticated user required"));
    }

    try {
      Map<String, Object> result =
          actionExecutorService.executeAction(
              request.getActionName(), userId, request.getParameters());
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error executing action", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  /** Extract user ID from JWT token */
  private Long extractUserId(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
      return null;
    }

    Jwt jwt = (Jwt) authentication.getPrincipal();

    // Try different claim names for userId
    Object userIdClaim = jwt.getClaim("userId");
    if (userIdClaim != null) {
      return Long.valueOf(userIdClaim.toString());
    }

    // Try sub claim as fallback
    String sub = jwt.getSubject();
    if (sub != null) {
      try {
        return Long.valueOf(sub);
      } catch (NumberFormatException e) {
        // If sub is a UUID, convert it to a Long using hashCode
        // This provides a stable numeric ID for the session
        log.info("Converting UUID sub to numeric userId: {}", sub);
        return (long) Math.abs(sub.hashCode());
      }
    }

    return null;
  }
}
