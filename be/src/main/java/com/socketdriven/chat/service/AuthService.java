package com.socketdriven.chat.service;

import com.socketdriven.chat.api.dto.AuthTokenPairResponse;
import com.socketdriven.chat.api.dto.LoginRequest;
import com.socketdriven.chat.api.dto.LoginResponse;
import com.socketdriven.chat.api.dto.LogoutRequest;
import com.socketdriven.chat.api.dto.RefreshTokenRequest;
import com.socketdriven.chat.api.dto.RegisterRequest;
import com.socketdriven.chat.domain.RefreshTokenSession;
import com.socketdriven.chat.domain.User;
import com.socketdriven.chat.repository.RefreshTokenSessionRepository;
import com.socketdriven.chat.repository.UserRepository;
import com.socketdriven.chat.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenSessionRepository refreshTokenSessionRepository;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      RefreshTokenSessionRepository refreshTokenSessionRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.refreshTokenSessionRepository = refreshTokenSessionRepository;
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
    if (userRepository.existsByEmail(req.email())) {
      throw new IllegalArgumentException("Email already taken");
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

  @Transactional
  public AuthTokenPairResponse refresh(RefreshTokenRequest req) {
    String raw = req.refreshToken();
    if (raw == null || raw.isBlank()) {
      throw new BadCredentialsException("Invalid refresh token");
    }
    if (!jwtService.isValid(raw) || !jwtService.isRefreshTokenClaims(raw)) {
      throw new BadCredentialsException("Invalid refresh token");
    }
    String jti = jwtService.parseJti(raw);
    UUID userId = jwtService.parseUserId(raw);
    RefreshTokenSession session =
        refreshTokenSessionRepository
            .findByJti(jti)
            .filter(s -> s.getRevokedAt() == null)
            .filter(s -> s.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    if (!session.getUser().getId().equals(userId)) {
      throw new BadCredentialsException("Invalid refresh token");
    }
    session.setRevokedAt(Instant.now());
    refreshTokenSessionRepository.save(session);
    User u = userRepository.findById(userId).orElseThrow();
    String newJti = UUID.randomUUID().toString();
    Instant exp = jwtService.refreshExpirationInstant();
    RefreshTokenSession next = new RefreshTokenSession();
    next.setUser(u);
    next.setJti(newJti);
    next.setExpiresAt(exp);
    refreshTokenSessionRepository.save(next);
    String access = jwtService.createAccessToken(u.getId(), u.getUsername());
    String refresh = jwtService.createRefreshToken(u.getId(), newJti);
    return new AuthTokenPairResponse(access, refresh);
  }

  @Transactional
  public void logoutWithRefreshTokenOnly(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    try {
      if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshTokenClaims(refreshToken)) {
        return;
      }
      String jti = jwtService.parseJti(refreshToken);
      refreshTokenSessionRepository
          .findByJti(jti)
          .ifPresent(
              s -> {
                s.setRevokedAt(Instant.now());
                refreshTokenSessionRepository.save(s);
              });
    } catch (Exception ignored) {
    }
  }

  @Transactional
  public void logout(UUID currentUserId, LogoutRequest req) {
    if (req.refreshToken() != null && !req.refreshToken().isBlank()) {
      try {
        if (jwtService.isValid(req.refreshToken())
            && jwtService.isRefreshTokenClaims(req.refreshToken())) {
          String jti = jwtService.parseJti(req.refreshToken());
          refreshTokenSessionRepository
              .findByJti(jti)
              .filter(s -> s.getUser().getId().equals(currentUserId))
              .ifPresent(
                  s -> {
                    s.setRevokedAt(Instant.now());
                    refreshTokenSessionRepository.save(s);
                  });
        }
      } catch (Exception ignored) {
      }
      return;
    }
    refreshTokenSessionRepository.findByUser_IdAndRevokedAtIsNull(currentUserId).stream()
        .forEach(
            s -> {
              s.setRevokedAt(Instant.now());
              refreshTokenSessionRepository.save(s);
            });
  }

  private LoginResponse toLoginResponse(User u) {
    String newJti = UUID.randomUUID().toString();
    Instant exp = jwtService.refreshExpirationInstant();
    RefreshTokenSession session = new RefreshTokenSession();
    session.setUser(u);
    session.setJti(newJti);
    session.setExpiresAt(exp);
    refreshTokenSessionRepository.save(session);
    String access = jwtService.createAccessToken(u.getId(), u.getUsername());
    String refresh = jwtService.createRefreshToken(u.getId(), newJti);
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
