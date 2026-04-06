package com.socketdriven.chat.api.dto;

import java.util.List;
import java.util.UUID;

public record CreateConversationRequest(
    String type, UUID otherUserId, String name, List<UUID> memberIds) {}
