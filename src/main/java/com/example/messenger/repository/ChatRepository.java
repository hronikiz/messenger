package com.example.messenger.repository;

import com.example.messenger.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE p.id = :userId")
    List<Chat> findAllByParticipantId(@Param("userId") Long userId);

    @Query("""
        SELECT c FROM Chat c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE p1.id = :userId1 AND p2.id = :userId2
        AND c.isGroup = false
    """)
    Optional<Chat> findPrivateChat(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
