package com.example.messenger.dto;

import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String username,
    String nickname,
    String email,
    boolean online,
    LocalDateTime lastSeen
) {}
