package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.LoginRequest;
import com.socketdriven.chat.api.dto.LoginResponse;
import com.socketdriven.chat.api.dto.RegisterRequest;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.UserRepository;
import com.socketdriven.chat.security.JwtService;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest req) {
    User u =
        userRepository
            .findByUsername(req.username())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }
    return toLoginResponse(u);
  }

  @Transactional
  public LoginResponse register(RegisterRequest req) {
    if (userRepository.existsByUsername(req.username())) {
      throw new IllegalArgumentException("Username already taken");
    }
    User u = new User();
    u.setUsername(req.username());
    u.setEmail(req.email());
    u.setPasswordHash(passwordEncoder.encode(req.password()));
    u.setDisplayName(
        Optional.ofNullable(req.displayName()).orElse(req.username()));
    userRepository.save(u);
    return login(new LoginRequest(req.username(), req.password()));
  }

  private LoginResponse toLoginResponse(User u) {
    String access = jwtService.createAccessToken(u.getId(), u.getUsername());
    String refresh = jwtService.createRefreshToken(u.getId());
    String dn = Optional.ofNullable(u.getDisplayName()).orElse(u.getUsername());
    String[] parts = dn.split(" ", 2);
    return new LoginResponse(
        access,
        refresh,
        u.getId().toString(),
        u.getUsername(),
        u.getEmail(),
        parts[0],
        parts.length > 1 ? parts[1] : "",
        "",
        Optional.ofNullable(u.getAvatarUrl()).orElse(""));
  }
}
