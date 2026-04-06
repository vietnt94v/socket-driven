package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);
}
