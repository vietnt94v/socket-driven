package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.LoginRequest;
import com.socketdriven.chat.api.dto.LoginResponse;
import com.socketdriven.chat.api.dto.RegisterRequest;
import com.socketdriven.chat.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/auth/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/auth/register")
  public LoginResponse register(@RequestBody RegisterRequest request) {
    return authService.register(request);
  }
}
