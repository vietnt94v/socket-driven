package com.socketdriven.chat.api;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Void> badCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> illegalArgument(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  }
}
