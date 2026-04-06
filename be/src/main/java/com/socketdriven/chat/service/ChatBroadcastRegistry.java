package com.socketdriven.chat.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatBroadcastRegistry {

  private final ConcurrentHashMap<UUID, Set<WebSocketSession>> byUser = new ConcurrentHashMap<>();

  public void add(UUID userId, WebSocketSession session) {
    byUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
  }

  public void remove(UUID userId, WebSocketSession session) {
    Set<WebSocketSession> set = byUser.get(userId);
    if (set != null) {
      set.remove(session);
      if (set.isEmpty()) {
        byUser.remove(userId, set);
      }
    }
  }

  public Set<UUID> sendToUsers(Iterable<UUID> userIds, String payload) {
    Set<UUID> delivered = new HashSet<>();
    for (UUID uid : userIds) {
      Set<WebSocketSession> sessions = byUser.get(uid);
      if (sessions == null) {
        continue;
      }
      for (WebSocketSession ws : sessions) {
        if (ws.isOpen()) {
          try {
            ws.sendMessage(new TextMessage(payload));
            delivered.add(uid);
          } catch (IOException ignored) {
          }
          break;
        }
      }
    }
    return delivered;
  }
}
