package com.example.stayfinder.repository.telegrambotchat;

import com.example.stayfinder.model.TelegramBotChat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramBotChatRepository extends JpaRepository<TelegramBotChat, Long> {
    Optional<TelegramBotChat> findByUserId(Long userId);
}
