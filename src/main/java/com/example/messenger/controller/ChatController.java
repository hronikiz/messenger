package com.example.messenger.controller;

import com.example.messenger.dto.ChatDTO;
import com.example.messenger.dto.MessageDTO;
import com.example.messenger.entity.Chat;
import com.example.messenger.entity.Message;
import com.example.messenger.service.ChatService;
import com.example.messenger.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;
    private final MessageService messageService;

    public ChatController(ChatService chatService, MessageService messageService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    // Создать чат
    @PostMapping("/create")
    public Chat createChat(@RequestBody ChatDTO dto) {
        return chatService.createChat(dto.getUserIds(), dto.getName());
    }

    // Получить все чаты пользователя
    @GetMapping("/user/{userId}")
    public List<Chat> getUserChats(@PathVariable Long userId) {
        return chatService.getUserChats(userId);
    }

    // Отправить сообщение в чат
    @PostMapping("/{chatId}/send")
    public Message sendMessage(@PathVariable Long chatId, @RequestBody MessageDTO dto) {
        return messageService.sendMessage(chatId, dto.getSenderId(), dto.getText());
    }

    // Получить все сообщения чата
    @GetMapping("/{chatId}/messages")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return messageService.getMessages(chatId);
    }
}