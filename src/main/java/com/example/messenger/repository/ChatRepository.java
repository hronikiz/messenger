package com.example.messenger.repository;

import com.example.messenger.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> { }