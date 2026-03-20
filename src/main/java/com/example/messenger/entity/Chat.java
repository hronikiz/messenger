package com.example.messenger.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // для групповых чатов, для личных можно оставить пустым

    @ManyToMany
    @JoinTable(
        name = "chat_users",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages;

    // ====== Геттеры и сеттеры ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}