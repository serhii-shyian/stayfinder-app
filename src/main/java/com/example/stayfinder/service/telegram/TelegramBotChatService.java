package com.example.stayfinder.service.telegram;

import com.example.stayfinder.model.TelegramBotChat;

public interface TelegramBotChatService {
    void saveChatId(Long chatId, Long userId);

    TelegramBotChat findByUserId(Long userId);

    boolean existsById(Long userId);
}
