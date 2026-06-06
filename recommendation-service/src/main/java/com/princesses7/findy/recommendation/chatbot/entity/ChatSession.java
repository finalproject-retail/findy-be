package com.princesses7.findy.recommendation.chatbot.entity;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession extends BaseTimeEntity {

	private static final int MAX_TITLE_LENGTH = 30;
	private static final int MAX_LAST_MESSAGE_LENGTH = 200;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatSessionId;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(length = 200)
	private String lastMessage;

	private ChatSession(Long userId, String title, String lastMessage) {
		this.userId = userId;
		this.title = title;
		this.lastMessage = lastMessage;
	}

	public static ChatSession create(Long userId, String firstMessage) {
		String title = createTitle(firstMessage);

		return new ChatSession(userId, title, firstMessage);
	}

	public void updateLastMessage(String message) {
		this.lastMessage = trimLastMessage(message);
	}

	private static String createTitle(String message) {
		if (message == null || message.isBlank()) {
			return "새 채팅";
		}

		String trimmedMessage = message.trim();

		if (trimmedMessage.length() <= MAX_TITLE_LENGTH) {
			return trimmedMessage;
		}

		return trimmedMessage.substring(0, MAX_TITLE_LENGTH);
	}

	private static String trimLastMessage(String message) {
		if (message == null || message.isBlank()) {
			return null;
		}

		String trimmedMessage = message.trim();

		if (trimmedMessage.length() <= MAX_LAST_MESSAGE_LENGTH) {
			return trimmedMessage;
		}

		return trimmedMessage.substring(0, MAX_LAST_MESSAGE_LENGTH);
	}
}