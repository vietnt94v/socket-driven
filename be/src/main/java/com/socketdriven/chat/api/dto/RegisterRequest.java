package com.socketdriven.chat.api.dto;

public record RegisterRequest(
    String username, String email, String password, String displayName) {}
