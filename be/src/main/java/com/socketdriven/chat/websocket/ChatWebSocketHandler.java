package com.socketdriven.chat.websocket;

import com.socketdriven.chat.domain.UserSession;
import com.socketdriven.chat.repository.UserRepository;
import com.socketdriven.chat.repository.UserSessionRepository;
import com.socketdriven.chat.service.ChatBroadcastRegistry;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

  private final ChatBroadcastRegistry chatBroadcastRegistry;
  private final UserSessionRepository userSessionRepository;
  private final UserRepository userRepository;

  public ChatWebSocketHandler(
      ChatBroadcastRegistry chatBroadcastRegistry,
      UserSessionRepository userSessionRepository,
      UserRepository userRepository) {
    this.chatBroadcastRegistry = chatBroadcastRegistry;
    this.userSessionRepository = userSessionRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    UUID userId = (UUID) session.getAttributes().get("userId");
    if (userId == null) {
      return;
    }
    chatBroadcastRegistry.add(userId, session);
    UserSession us = new UserSession();
    us.setUser(userRepository.getReferenceById(userId));
    us.setSocketId(session.getId());
    userSessionRepository.save(us);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    UUID userId = (UUID) session.getAttributes().get("userId");
    if (userId != null) {
      chatBroadcastRegistry.remove(userId, session);
    }
    userSessionRepository
        .findBySocketId(session.getId())
        .ifPresent(
            us -> {
              us.setActive(false);
              us.setDisconnectedAt(Instant.now());
              userSessionRepository.save(us);
            });
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    String p = message.getPayload();
    if ("ping".equalsIgnoreCase(p.trim())) {
      try {
        session.sendMessage(new TextMessage("pong"));
      } catch (Exception ignored) {
      }
    }
  }
}
