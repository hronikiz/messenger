package com.example.messenger.dto;

public record AuthResponse(
    String token,
    UserDTO user
) {}
