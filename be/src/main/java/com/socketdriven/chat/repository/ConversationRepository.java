package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.Conversation;
import com.socketdriven.chat.domain.ConversationType;
import java.util.List;
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

  @Query(
      """
      SELECT DISTINCT c FROM Conversation c
      JOIN ConversationMember cmSelf ON cmSelf.conversation = c
          AND cmSelf.user.id = :meId AND cmSelf.active = true
      WHERE c.type = :groupType
      AND (
          LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          OR EXISTS (
              SELECT 1 FROM ConversationMember m2
              WHERE m2.conversation = c AND m2.active = true
              AND m2.user.id <> :meId
              AND (
                  LOWER(m2.user.username) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(COALESCE(m2.user.displayName, '')) LIKE LOWER(CONCAT('%', :q, '%'))
              )
          )
      )
      """)
  List<Conversation> searchGroupsForMember(
      @Param("meId") UUID meId, @Param("q") String q, @Param("groupType") ConversationType groupType);
}
