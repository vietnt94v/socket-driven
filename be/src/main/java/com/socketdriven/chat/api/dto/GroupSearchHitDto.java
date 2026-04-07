package com.socketdriven.chat.api.dto;

import java.util.UUID;

public record GroupSearchHitDto(
    UUID id, String name, String avatarUrl, int memberCount) {}
