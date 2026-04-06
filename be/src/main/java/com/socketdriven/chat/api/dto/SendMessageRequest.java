package com.socketdriven.chat.api.dto;

import java.util.UUID;

public record SendMessageRequest(String content, UUID replyToId) {}
