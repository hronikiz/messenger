package com.example.messenger.controller;

import com.example.messenger.dto.*;
import com.example.messenger.service.ChatService;
import com.example.messenger.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // Создать личный чат
    @PostMapping("/private")
    public ResponseEntity<ChatResponse> createPrivateChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePrivateChatRequest request) {

        Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(chatService.createPrivateChat(currentUserId, request));
    }

    // Создать групповой чат
    @PostMapping("/group")
    public ResponseEntity<ChatResponse> createGroupChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGroupChatRequest request) {

        Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(chatService.createGroupChat(currentUserId, request));
    }

    // Получить все свои чаты
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getMyChats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(chatService.getUserChats(currentUserId));
    }

    // Получить один чат
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(chatService.getChat(chatId, currentUserId));
    }

    // Добавить участника в групповой чат
    @PostMapping("/{chatId}/participants/{userId}")
    public ResponseEntity<ChatResponse> addParticipant(
            @PathVariable Long chatId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = userService.getEntityByEmail(userDetails.getUsername()).getId();
        return ResponseEntity.ok(chatService.addParticipant(chatId, currentUserId, userId));
    }
}
