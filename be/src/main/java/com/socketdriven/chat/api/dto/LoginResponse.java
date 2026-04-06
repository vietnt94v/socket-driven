package com.socketdriven.chat.api.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    String gender,
    String image) {}
