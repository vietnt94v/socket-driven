package com.socketdriven.chat.api.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(
    UUID id,
    UUID conversationId,
    UUID senderId,
    String type,
    String content,
    UUID replyToId,
    boolean deleted,
    Instant createdAt,
    Instant updatedAt) {}
