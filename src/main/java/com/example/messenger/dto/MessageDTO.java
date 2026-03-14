package com.example.messenger.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageDTO {

    private Long senderId;
    private Long receiverId;

    @NotBlank
    private String text;

    public MessageDTO() {}

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getText() {
        return text;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public void setText(String text) {
        this.text = text;
    }
}