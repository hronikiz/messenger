package com.example.messenger.service;

import com.example.messenger.dto.*;
import com.example.messenger.entity.Chat;
import com.example.messenger.entity.User;
import com.example.messenger.exception.ConflictException;
import com.example.messenger.exception.ForbiddenException;
import com.example.messenger.exception.NotFoundException;
import com.example.messenger.repository.ChatRepository;
import com.example.messenger.repository.MessageRepository;
import com.example.messenger.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final MessageService messageService;

    // Создать личный чат (или вернуть существующий)
    @Transactional
    public ChatResponse createPrivateChat(Long currentUserId, CreatePrivateChatRequest request) {
        if (currentUserId.equals(request.targetUserId())) {
            throw new ConflictException("Нельзя создать чат с самим собой");
        }

        // Если личный чат уже существует — вернём его
        return chatRepository.findPrivateChat(currentUserId, request.targetUserId())
            .map(existingChat -> toDTO(existingChat, currentUserId))
            .orElseGet(() -> {
                User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
                User targetUser = userRepository.findById(request.targetUserId())
                    .orElseThrow(() -> new NotFoundException("Целевой пользователь не найден"));

                Chat chat = new Chat();
                chat.setGroup(false);
                chat.setParticipants(List.of(currentUser, targetUser));
                chatRepository.save(chat);

                return toDTO(chat, currentUserId);
            });
    }

    // Создать групповой чат
    @Transactional
    public ChatResponse createGroupChat(Long currentUserId, CreateGroupChatRequest request) {
        List<Long> allIds = new ArrayList<>(request.participantIds());
        if (!allIds.contains(currentUserId)) {
            allIds.add(currentUserId);
        }

        List<User> participants = userRepository.findAllById(allIds);
        if (participants.size() < 2) {
            throw new ConflictException("В групповом чате должно быть минимум 2 участника");
        }

        Chat chat = new Chat();
        chat.setName(request.name());
        chat.setGroup(true);
        chat.setParticipants(participants);
        chatRepository.save(chat);

        return toDTO(chat, currentUserId);
    }

    // Получить все чаты пользователя
    @Transactional(readOnly = true)
    public List<ChatResponse> getUserChats(Long userId) {
        return chatRepository.findAllByParticipantId(userId).stream()
            .map(chat -> toDTO(chat, userId))
            .toList();
    }

    // Получить один чат (с проверкой доступа)
    @Transactional(readOnly = true)
    public ChatResponse getChat(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Чат не найден"));

        boolean isMember = chat.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(userId));

        if (!isMember) {
            throw new ForbiddenException("У вас нет доступа к этому чату");
        }

        return toDTO(chat, userId);
    }

    // Добавить участника в групповой чат
    @Transactional
    public ChatResponse addParticipant(Long chatId, Long userId, Long newUserId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Чат не найден"));

        if (!chat.isGroup()) {
            throw new ConflictException("Нельзя добавить участника в личный чат");
        }

        boolean isMember = chat.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(userId));
        if (!isMember) {
            throw new ForbiddenException("У вас нет доступа к этому чату");
        }

        User newUser = userRepository.findById(newUserId)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        boolean alreadyIn = chat.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(newUserId));
        if (alreadyIn) {
            throw new ConflictException("Пользователь уже в чате");
        }

        chat.getParticipants().add(newUser);
        chatRepository.save(chat);

        return toDTO(chat, userId);
    }

    // Маппинг Chat → ChatResponse
    private ChatResponse toDTO(Chat chat, Long currentUserId) {
        List<UserDTO> participants = chat.getParticipants().stream()
            .map(userService::toDTO)
            .toList();

        // Последнее сообщение
        var lastMessages = messageRepository
            .findByChatIdOrderByTimestampDesc(chat.getId(), PageRequest.of(0, 1));
        MessageResponse lastMessage = lastMessages.isEmpty()
            ? null
            : messageService.toDTO(lastMessages.getContent().get(0));

        // Количество непрочитанных
        long unreadCount = messageRepository
            .countByChatIdAndIsReadFalseAndSenderIdNot(chat.getId(), currentUserId);

        return new ChatResponse(
            chat.getId(),
            chat.getName(),
            chat.isGroup(),
            participants,
            lastMessage,
            unreadCount,
            chat.getCreatedAt()
        );
    }
}