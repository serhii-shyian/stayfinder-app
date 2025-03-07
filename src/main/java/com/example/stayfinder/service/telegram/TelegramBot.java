package com.example.stayfinder.service.telegram;

import com.example.stayfinder.exception.DataProcessingException;
import com.example.stayfinder.model.TelegramBotChat;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private final TelegramBotChatService telegramBotChatService;
    @Value("${bot.email.regex}")
    private String emailRegex;
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.key}")
    private String token;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            if (messageText.equalsIgnoreCase("/start")) {
                handleStartCommand(chatId, userName);
            } else {
                SendMessage sendMessage = checkUpdateText(update);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new DataProcessingException("Can't send message to telegram!", e);
                }
            }
        }
    }

    private void handleStartCommand(Long chatId, String userName) {
        sendMessage(chatId, "Hi, " + userName
                + "! You have successfully started using the chat. "
                + "Please enter your registered email.");
    }

    public void sendNotification(String message, Long userId) {
        if (telegramBotChatService.existsById(userId)) {
            TelegramBotChat botChat = telegramBotChatService.findByUserId(userId);
            sendMessage(botChat.getChatId(), message);
        } else {
            System.out.println("No chat registered for this user.");
        }
    }

    public void sendNotificationToUsers(String message, List<User> users) {
        for (User user : users) {
            if (telegramBotChatService.existsById(user.getId())) {
                TelegramBotChat botChat = telegramBotChatService.findByUserId(user.getId());
                sendMessage(botChat.getChatId(), message);
            }
        }
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(messageText);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new DataProcessingException("Could not send a message to chat id: " + chatId);
        }
    }

    private SendMessage checkUpdateText(Update update) {
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());

        if (Pattern.compile(emailRegex).matcher(message.getText()).matches()) {
            String email = message.getText();
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                telegramBotChatService.saveChatId(message.getChatId(), userOptional.get().getId());
                sendMessage.setText("Thank you! Now you will receive project notifications here.");
            } else {
                sendMessage.setText("No user found with this email, please try again.");
            }
        } else if (message.getText().equalsIgnoreCase("/start")) {
            sendMessage.setText("Hello, please enter the email registered with the service.");
        }

        return sendMessage;
    }
}
