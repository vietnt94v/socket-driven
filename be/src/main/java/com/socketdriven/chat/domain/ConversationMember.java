package com.socketdriven.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "conversation_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"conversation_id", "user_id"}))
public class ConversationMember {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "conversation_id", nullable = false)
  private Conversation conversation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MemberRole role;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt = Instant.now();

  @Column(name = "left_at")
  private Instant leftAt;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "added_by")
  private User addedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "removed_by")
  private User removedBy;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Conversation getConversation() {
    return conversation;
  }

  public void setConversation(Conversation conversation) {
    this.conversation = conversation;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public MemberRole getRole() {
    return role;
  }

  public void setRole(MemberRole role) {
    this.role = role;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(Instant joinedAt) {
    this.joinedAt = joinedAt;
  }

  public Instant getLeftAt() {
    return leftAt;
  }

  public void setLeftAt(Instant leftAt) {
    this.leftAt = leftAt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public User getAddedBy() {
    return addedBy;
  }

  public void setAddedBy(User addedBy) {
    this.addedBy = addedBy;
  }

  public User getRemovedBy() {
    return removedBy;
  }

  public void setRemovedBy(User removedBy) {
    this.removedBy = removedBy;
  }
}
