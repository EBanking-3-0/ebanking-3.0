package com.ebanking.account.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
    log.error("Unhandled exception occurred in Account Service: ", ex);
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", java.time.Instant.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage());
    body.put("type", ex.getClass().getSimpleName());

    if (ex.getCause() != null) {
      body.put("cause", ex.getCause().getMessage());
    }

    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(AccountNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleAccountNotFound(AccountNotFoundException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(InsufficientBalance.class)
  public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalance ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }
}
