package com.example.stayfinder.service.telegram;

import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.model.TelegramBotChat;
import com.example.stayfinder.repository.telegrambotchat.TelegramBotChatRepository;
import com.example.stayfinder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramBotChatServiceImpl implements TelegramBotChatService {
    private final TelegramBotChatRepository botRepository;
    private final UserRepository userRepository;

    @Override
    public void saveChatId(Long chatId, Long userId) {
        if (userRepository.existsById(userId)) {
            botRepository.save(createTelegramBotChat(chatId, userId));
        }
    }

    @Override
    public TelegramBotChat findByUserId(Long userId) {
        return botRepository.findByUserId(userId).orElseThrow(
                () -> new EntityNotFoundException(
                        "Can't find TaskSystemBotChat by user ID: " + userId));
    }

    @Override
    public boolean existsById(Long userId) {
        return botRepository.findByUserId(userId).isPresent();
    }

    private TelegramBotChat createTelegramBotChat(Long chatId, Long userId) {
        return new TelegramBotChat()
                .setChatId(chatId)
                .setUserId(userId);
    }
}
