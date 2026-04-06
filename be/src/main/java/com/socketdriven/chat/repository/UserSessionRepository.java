package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.UserSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  Optional<UserSession> findBySocketId(String socketId);
}
