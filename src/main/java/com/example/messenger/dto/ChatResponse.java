package com.example.messenger.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
    Long id,
    String name,
    boolean isGroup,
    List<UserDTO> participants,
    MessageResponse lastMessage,
    long unreadCount,
    LocalDateTime createdAt
) {}
