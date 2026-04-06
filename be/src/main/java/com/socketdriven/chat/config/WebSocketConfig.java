package com.socketdriven.chat.config;

import com.socketdriven.chat.websocket.ChatWebSocketHandler;
import com.socketdriven.chat.websocket.JwtHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final ChatWebSocketHandler chatWebSocketHandler;
  private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  public WebSocketConfig(
      ChatWebSocketHandler chatWebSocketHandler,
      JwtHandshakeInterceptor jwtHandshakeInterceptor) {
    this.chatWebSocketHandler = chatWebSocketHandler;
    this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    String[] origins = allowedOrigins.split(",");
    registry
        .addHandler(chatWebSocketHandler, "/ws/chat")
        .addInterceptors(jwtHandshakeInterceptor)
        .setAllowedOrigins(origins);
  }
}
