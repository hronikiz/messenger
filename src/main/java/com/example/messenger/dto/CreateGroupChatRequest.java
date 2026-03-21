package com.example.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGroupChatRequest(
    @NotBlank(message = "Название чата обязательно")
    String name,

    @NotNull
    @Size(min = 1, message = "Добавьте хотя бы одного участника")
    List<Long> participantIds
) {}
