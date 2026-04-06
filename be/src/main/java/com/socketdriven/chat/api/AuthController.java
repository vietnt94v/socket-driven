package com.socketdriven.chat.api;

import com.socketdriven.chat.api.dto.AuthTokenPairResponse;
import com.socketdriven.chat.api.dto.LoginRequest;
import com.socketdriven.chat.api.dto.LoginResponse;
import com.socketdriven.chat.api.dto.LogoutRequest;
import com.socketdriven.chat.api.dto.RefreshTokenRequest;
import com.socketdriven.chat.api.dto.RegisterRequest;
import com.socketdriven.chat.service.AuthService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/register")
  public LoginResponse register(@RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/refresh")
  public AuthTokenPairResponse refresh(@RequestBody RefreshTokenRequest request) {
    return authService.refresh(request);
  }

  @PostMapping("/logout")
  public void logout(
      @RequestBody(required = false) LogoutRequest request, Authentication authentication) {
    if (authentication != null
        && authentication.isAuthenticated()
        && authentication.getPrincipal() instanceof String principal
        && !principal.isBlank()) {
      authService.logout(
          UUID.fromString(principal), request != null ? request : new LogoutRequest(null));
    } else {
      authService.logoutWithRefreshTokenOnly(
          request != null ? request.refreshToken() : null);
    }
  }
}
