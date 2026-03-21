package com.example.messenger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username не может быть пустым")
    String username,

    @NotBlank(message = "Nickname не может быть пустым")
    @Size(min = 3, max = 32, message = "Nickname: от 3 до 32 символов")
    String nickname,

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    String email,

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль: минимум 6 символов")
    String password
) {}
