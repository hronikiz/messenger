package com.example.messenger.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePrivateChatRequest(
    @NotNull(message = "ID пользователя обязателен")
    Long targetUserId
) {}
