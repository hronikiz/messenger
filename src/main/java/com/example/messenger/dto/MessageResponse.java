package com.example.messenger.dto;

import com.example.messenger.entity.Message;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id,
    Long chatId,
    UserDTO sender,
    String text,
    boolean isRead,
    Message.MessageType type,
    LocalDateTime timestamp
) {}
