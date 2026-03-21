package com.example.messenger.controller;

import com.example.messenger.dto.MessageResponse;
import com.example.messenger.dto.SendMessageRequest;
import com.example.messenger.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ─── REST: История сообщений (с пагинацией) ───────────────────────

    @GetMapping("/api/chats/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
            messageService.getMessages(chatId, userDetails.getUsername(), page, size)
        );
    }

    // Отметить сообщения прочитанными (REST)
    @PutMapping("/api/chats/{chatId}/messages/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {

        messageService.markAsRead(chatId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ─── WebSocket: Отправка сообщений real-time ──────────────────────
    // Клиент отправляет на: /app/chat.{chatId}
    // Все в чате получают на: /topic/chat.{chatId}

    @MessageMapping("/chat.{chatId}")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload @Valid SendMessageRequest request,
            Principal principal) {

        messageService.sendMessage(chatId, principal.getName(), request);
        // SimpMessagingTemplate.convertAndSend вызывается внутри MessageService
    }
}
