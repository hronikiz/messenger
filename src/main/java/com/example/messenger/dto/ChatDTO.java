package com.example.messenger.dto;

import java.util.List;

public class ChatDTO {

    private List<Long> userIds; // ID участников
    private String name;        // имя чата (для группового)

    // ====== Геттеры и сеттеры ======
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}