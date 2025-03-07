package com.example.stayfinder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "telegram_bot_chats")
@SQLDelete(sql = "UPDATE telegram_bot_chats SET is_deleted = true WHERE id = ?")
@SQLRestriction(value = "is_deleted=false")
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class TelegramBotChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long chatId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private boolean isDeleted;
}
