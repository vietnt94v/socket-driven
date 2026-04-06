package com.socketdriven.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
public class UserSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "socket_id", nullable = false, length = 100)
  private String socketId;

  @Column(name = "device_info", length = 200)
  private String deviceInfo;

  @Column(name = "connected_at", nullable = false)
  private Instant connectedAt = Instant.now();

  @Column(name = "disconnected_at")
  private Instant disconnectedAt;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getSocketId() {
    return socketId;
  }

  public void setSocketId(String socketId) {
    this.socketId = socketId;
  }

  public String getDeviceInfo() {
    return deviceInfo;
  }

  public void setDeviceInfo(String deviceInfo) {
    this.deviceInfo = deviceInfo;
  }

  public Instant getConnectedAt() {
    return connectedAt;
  }

  public void setConnectedAt(Instant connectedAt) {
    this.connectedAt = connectedAt;
  }

  public Instant getDisconnectedAt() {
    return disconnectedAt;
  }

  public void setDisconnectedAt(Instant disconnectedAt) {
    this.disconnectedAt = disconnectedAt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
