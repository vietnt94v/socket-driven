package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.Conversation;
import com.socketdriven.chat.domain.ConversationType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  @Query(
      """
      SELECT DISTINCT c FROM Conversation c
      WHERE c.type = :dtype
      AND EXISTS (
        SELECT 1 FROM ConversationMember m1
        WHERE m1.conversation = c AND m1.user.id = :a AND m1.active = true)
      AND EXISTS (
        SELECT 1 FROM ConversationMember m2
        WHERE m2.conversation = c AND m2.user.id = :b AND m2.active = true)
      """)
  Optional<Conversation> findDirectBetween(
      @Param("a") UUID userA,
      @Param("b") UUID userB,
      @Param("dtype") ConversationType dtype);
}
