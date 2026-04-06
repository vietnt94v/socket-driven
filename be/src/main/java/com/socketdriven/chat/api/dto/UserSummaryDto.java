package com.socketdriven.chat.api.dto;

import java.util.UUID;

public record UserSummaryDto(
    UUID id, String username, String displayName, String firstName, String lastName, String image) {}
