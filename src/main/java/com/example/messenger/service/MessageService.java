package com.example.messenger.service;

import com.example.messenger.entity.Chat;
import com.example.messenger.entity.Message;
import com.example.messenger.entity.User;
import com.example.messenger.repository.ChatRepository;
import com.example.messenger.repository.MessageRepository;
import com.example.messenger.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          ChatRepository chatRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    public Message sendMessage(Long chatId, Long senderId, String text) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found"));

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setText(text);

        return messageRepository.save(message);
    }

    public List<Message> getMessages(Long chatId) {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }
}