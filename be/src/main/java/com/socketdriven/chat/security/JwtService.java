package com.socketdriven.chat.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long accessMinutes;
  private final long refreshDays;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-minutes}") long accessMinutes,
      @Value("${app.jwt.refresh-days}") long refreshDays) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessMinutes = accessMinutes;
    this.refreshDays = refreshDays;
  }

  public String createAccessToken(UUID userId, String username) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessMinutes * 60)))
        .signWith(key)
        .compact();
  }

  public String createRefreshToken(UUID userId, String jti) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(jti)
        .subject(userId.toString())
        .claim("typ", "refresh")
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(refreshDays * 24 * 60 * 60)))
        .signWith(key)
        .compact();
  }

  public Instant refreshExpirationInstant() {
    return Instant.now().plusSeconds(refreshDays * 24 * 60 * 60);
  }

  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public UUID parseUserId(String token) {
    return UUID.fromString(parseClaims(token).getSubject());
  }

  public String parseJti(String token) {
    String id = parseClaims(token).getId();
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("missing jti");
    }
    return id;
  }

  public boolean isRefreshTokenClaims(String token) {
    try {
      Claims c = parseClaims(token);
      return "refresh".equals(c.get("typ"));
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
