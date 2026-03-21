package com.example.messenger.service;

import com.example.messenger.dto.MessageResponse;
import com.example.messenger.dto.SendMessageRequest;
import com.example.messenger.entity.Chat;
import com.example.messenger.entity.Message;
import com.example.messenger.entity.User;
import com.example.messenger.exception.ForbiddenException;
import com.example.messenger.exception.NotFoundException;
import com.example.messenger.repository.ChatRepository;
import com.example.messenger.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;
    // WebSocket шаблон для отправки real-time сообщений
    private final SimpMessagingTemplate messagingTemplate;

    // Отправить сообщение
    @Transactional
    public MessageResponse sendMessage(Long chatId, String senderEmail, SendMessageRequest request) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Чат не найден"));

        User sender = userService.getEntityByEmail(senderEmail);

        // Проверка: отправитель — участник чата
        boolean isMember = chat.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(sender.getId()));
        if (!isMember) {
            throw new ForbiddenException("Вы не являетесь участником этого чата");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setText(request.text());
        message.setType(request.type() != null ? request.type() : Message.MessageType.TEXT);

        messageRepository.save(message);

        MessageResponse response = toDTO(message);

        // Real-time доставка через WebSocket всем участникам чата
        messagingTemplate.convertAndSend("/topic/chat." + chatId, response);

        return response;
    }

    // Получить историю сообщений (с пагинацией)
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long chatId, String userEmail, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new NotFoundException("Чат не найден"));

        User user = userService.getEntityByEmail(userEmail);

        boolean isMember = chat.getParticipants().stream()
            .anyMatch(p -> p.getId().equals(user.getId()));
        if (!isMember) {
            throw new ForbiddenException("У вас нет доступа к этому чату");
        }

        return messageRepository
            .findByChatIdOrderByTimestampDesc(chatId, PageRequest.of(page, size))
            .map(this::toDTO);
    }

    // Отметить все сообщения чата как прочитанные
    @Transactional
    public void markAsRead(Long chatId, String userEmail) {
        User user = userService.getEntityByEmail(userEmail);
        messageRepository.markAllAsRead(chatId, user.getId());

        // Уведомить других участников о прочтении через WebSocket
        messagingTemplate.convertAndSend(
            "/topic/chat." + chatId + ".read",
            new ReadStatusEvent(chatId, user.getId())
        );
    }

    // Маппинг Message → MessageResponse
    public MessageResponse toDTO(Message message) {
        return new MessageResponse(
            message.getId(),
            message.getChat().getId(),
            userService.toDTO(message.getSender()),
            message.getText(),
            message.isRead(),
            message.getType(),
            message.getTimestamp()
        );
    }

    // Событие прочтения для WebSocket
    public record ReadStatusEvent(Long chatId, Long userId) {}
}
