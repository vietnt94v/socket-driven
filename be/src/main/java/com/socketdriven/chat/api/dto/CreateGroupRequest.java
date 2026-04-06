package com.socketdriven.chat.api.dto;

import java.util.List;
import java.util.UUID;

public record CreateGroupRequest(String name, List<UUID> memberIds) {}
