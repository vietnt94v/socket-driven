package com.socketdriven.chat.repository;

import com.socketdriven.chat.domain.ConversationMember;
import com.socketdriven.chat.domain.MemberRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

  @Query(
      """
      SELECT cm FROM ConversationMember cm JOIN cm.conversation c
      WHERE cm.user.id = :uid AND cm.active = true
      ORDER BY c.lastMessageAt DESC NULLS LAST
      """)
  List<ConversationMember> findActiveForUserOrderByLastMessage(@Param("uid") UUID userId);

  List<ConversationMember> findByConversation_IdAndActiveTrue(UUID conversationId);

  Optional<ConversationMember> findByConversation_IdAndUser_Id(UUID conversationId, UUID userId);

  long countByConversation_IdAndActiveTrue(UUID conversationId);

  long countByConversation_IdAndActiveTrueAndRole(
      UUID conversationId, MemberRole role);

  @Query(
      """
      SELECT m FROM ConversationMember m
      WHERE m.conversation.id = :cid AND m.active = true AND m.role = :role
      """)
  List<ConversationMember> findActiveByConversationAndRole(
      @Param("cid") UUID conversationId, @Param("role") MemberRole role);

  @Query(
      """
      SELECT m FROM ConversationMember m
      WHERE m.conversation.id = :cid AND m.active = true
      ORDER BY m.joinedAt ASC
      """)
  List<ConversationMember> findActiveByConversationOrderByJoinedAt(
      @Param("cid") UUID conversationId);
}
