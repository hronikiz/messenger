package com.example.messenger.repository;

import com.example.messenger.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

    List<Message> findByChatIdAndIsReadFalseAndSenderIdNot(Long chatId, Long currentUserId);

    long countByChatIdAndIsReadFalseAndSenderIdNot(Long chatId, Long currentUserId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.isRead = false")
    void markAllAsRead(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
