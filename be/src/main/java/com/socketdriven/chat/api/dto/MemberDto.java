package com.socketdriven.chat.api.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberDto(
    UUID userId,
    String username,
    String displayName,
    String role,
    Instant joinedAt) {}
