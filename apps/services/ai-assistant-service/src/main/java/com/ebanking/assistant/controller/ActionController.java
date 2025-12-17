package com.ebanking.assistant.controller;

import com.ebanking.assistant.model.ActionRequest;
import com.ebanking.assistant.service.ActionExecutorService;
import com.ebanking.assistant.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for direct action execution (admin/testing purposes).
 * In production, this should be restricted to admin users only.
 */
@Slf4j
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ActionController {
    
    private final ActionExecutorService actionExecutorService;
    private final SecurityUtil securityUtil;
    
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeAction(
            @Valid @RequestBody ActionRequest request,
            HttpServletRequest httpRequest) {
        
        String userId = securityUtil.extractUserIdFromHeader(httpRequest);
        if (userId == null) {
            userId = request.getUserId() != null ? request.getUserId().toString() : null; // Fallback to request userId
        }
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User ID is required"));
        }
        
        try {
            Map<String, Object> result = actionExecutorService.executeAction(
                    request.getActionName(),
                    userId,
                    request.getParameters()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing action", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
