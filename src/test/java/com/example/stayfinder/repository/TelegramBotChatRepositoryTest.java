package com.example.stayfinder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stayfinder.model.TelegramBotChat;
import com.example.stayfinder.repository.telegrambotchat.TelegramBotChatRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TelegramBotChatRepositoryTest {

    @Autowired
    private TelegramBotChatRepository telegramBotChatRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/delete-all-data-before-tests.sql"));
        }
    }

    @Test
    @DisplayName("""
            Find TelegramBotChat by existing userId
            """)
    @Sql(scripts = {
            "classpath:database/telegram_bot_chats/insert-into-telegram-bot-chats.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/telegram_bot_chats/delete-all-from-telegram-bot-chats.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserId_ExistingUserId_ReturnsTelegramBotChat() {
        // Given
        Long userId = 1L;

        // When
        Optional<TelegramBotChat> optionalBotChat
                = telegramBotChatRepository.findByUserId(userId);

        // Then
        assertTrue(optionalBotChat.isPresent());
        assertEquals(userId, optionalBotChat.get().getUserId());
    }

    @Test
    @DisplayName("""
            Find TelegramBotChat by non-existing userId
            """)
    @Sql(scripts = {
            "classpath:database/telegram_bot_chats/insert-into-telegram-bot-chats.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/telegram_bot_chats/delete-all-from-telegram-bot-chats.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserId_NonExistingUserId_ReturnsEmptyOptional() {
        // Given
        Long userId = 99L;

        // When
        Optional<TelegramBotChat> optionalBotChat
                = telegramBotChatRepository.findByUserId(userId);

        // Then
        assertFalse(optionalBotChat.isPresent());
    }
}
