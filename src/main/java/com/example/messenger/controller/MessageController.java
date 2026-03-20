package com.example.messenger.controller;

import com.example.messenger.dto.MessageDTO;
import com.example.messenger.entity.Message;
import com.example.messenger.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/{chatId}/send")
    public Message sendMessage(
            @PathVariable Long chatId,
            @RequestBody MessageDTO dto
    ) {

        return messageService.sendMessage(chatId, dto.getSenderId(), dto.getText());
    }

    @GetMapping("/{chatId}")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return messageService.getMessages(chatId);
    }
}