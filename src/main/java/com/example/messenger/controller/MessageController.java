package com.example.messenger.controller;

import com.example.messenger.dto.MessageDTO;
import com.example.messenger.model.Message;
import com.example.messenger.repository.MessageRepository;

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
    public Message sendMessage(@RequestBody MessageDTO dto) {

        Message message = new Message(
                dto.getSenderId(),
                dto.getReceiverId(),
                dto.getText()
        );

        return messageRepository.save(message);
    }

    @GetMapping("/chat")
    public List<Message> getChat(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {

        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }
}