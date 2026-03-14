package com.example.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MessageDTO {

    @NotNull
    private Long chatId;

    @NotNull
    private Long senderId;

    @NotBlank
    private String text;

    public MessageDTO() {}

    public Long getChatId() {
        return chatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setText(String text) {
        this.text = text;
    }
}