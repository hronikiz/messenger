package com.example.messenger.dto;

import com.example.messenger.entity.Message;
import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
    @NotBlank(message = "Текст сообщения не может быть пустым")
    String text,

    Message.MessageType type
) {}
