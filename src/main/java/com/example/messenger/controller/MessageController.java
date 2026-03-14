package com.example.messenger.controller;

import com.example.messenger.dto.MessageDTO;
import com.example.messenger.model.Message;
import com.example.messenger.repository.MessageRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping("/send")
    public Message sendMessage(@Valid @RequestBody MessageDTO dto) {

        Message message = new Message();
        message.setChatId(dto.getChatId());
        message.setSenderId(dto.getSenderId());
        message.setText(dto.getText());

        return messageRepository.save(message);
    }

    @GetMapping("/chat/{chatId}")
    public List<Message> getChatMessages(@PathVariable Long chatId) {

        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }
}