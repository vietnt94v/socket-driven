package com.socketdriven.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "message_status")
public class MessageStatus {

  @EmbeddedId
  private MessageStatusId id = new MessageStatusId();

  @MapsId("messageId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "message_id", nullable = false)
  private Message message;

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "read_at")
  private Instant readAt;

  public MessageStatusId getId() {
    return id;
  }

  public void setId(MessageStatusId id) {
    this.id = id;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
    if (message != null && id != null) {
      id.setMessageId(message.getId());
    }
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
    if (user != null && id != null) {
      id.setUserId(user.getId());
    }
  }

  public Instant getDeliveredAt() {
    return deliveredAt;
  }

  public void setDeliveredAt(Instant deliveredAt) {
    this.deliveredAt = deliveredAt;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public void setReadAt(Instant readAt) {
    this.readAt = readAt;
  }
}
