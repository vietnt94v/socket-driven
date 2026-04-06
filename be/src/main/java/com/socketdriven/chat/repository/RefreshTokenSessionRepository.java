package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.RefreshTokenSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {

  Optional<RefreshTokenSession> findByJti(String jti);

  List<RefreshTokenSession> findByUser_IdAndRevokedAtIsNull(UUID userId);
}
