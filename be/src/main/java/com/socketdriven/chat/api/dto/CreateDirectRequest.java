package com.socketdriven.chat.api.dto;

import java.util.UUID;

public record CreateDirectRequest(UUID otherUserId) {}
