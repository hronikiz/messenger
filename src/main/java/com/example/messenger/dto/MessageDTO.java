package com.example.messenger.dto;

public class MessageDTO {

    private Long senderId; 
    private String text;   

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}