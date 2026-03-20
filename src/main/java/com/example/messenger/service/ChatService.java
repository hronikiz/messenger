package com.example.messenger.service;

import com.example.messenger.entity.Chat;
import com.example.messenger.entity.User;
import com.example.messenger.repository.ChatRepository;
import com.example.messenger.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public Chat createChat(List<Long> userIds, String name) {
        Chat chat = new Chat();
        chat.setName(name);

        List<User> participants = userRepository.findAllById(userIds);
        chat.setParticipants(participants);

        return chatRepository.save(chat);
    }

    public List<Chat> getUserChats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getChats();
    }
}