package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Query(
      """
      SELECT u FROM User u
      WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
         OR LOWER(COALESCE(u.displayName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
      ORDER BY u.username
      """)
  List<User> searchByQuery(@Param("q") String q);
}
