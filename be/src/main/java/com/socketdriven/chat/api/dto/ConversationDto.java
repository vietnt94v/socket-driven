package com.socketdriven.chat.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationDto(
    UUID id,
    String type,
    String name,
    String avatarUrl,
    Instant lastMessageAt) {}
