package com.princesses7.findy.recommendation.chatbot.log.entity;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;
import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chatbot_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatbotLog extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatbotLogId;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "chat_session_id")
	private Long chatSessionId;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ChatIntent intent;

	@Column(length = 255)
	private String keyword;

	@Column(name = "request_message", nullable = false, columnDefinition = "TEXT")
	private String requestMessage;

	@Column(name = "response_message", columnDefinition = "TEXT")
	private String responseMessage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChatbotLogStatus status;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "failure_type", length = 50)
	private ChatbotFailureType failureType;

	@Column(name = "fallback_message", columnDefinition = "TEXT")
	private String fallbackMessage;

	@Column(name = "duration_ms", nullable = false)
	private Long durationMs;

	private ChatbotLog(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		String responseMessage,
		ChatbotLogStatus status,
		String failureReason,
		ChatbotFailureType failureType,
		String fallbackMessage,
		Long durationMs
	) {
		this.userId = userId;
		this.chatSessionId = chatSessionId;
		this.intent = intent;
		this.keyword = keyword;
		this.requestMessage = requestMessage;
		this.responseMessage = responseMessage;
		this.status = status;
		this.failureReason = failureReason;
		this.failureType = failureType;
		this.fallbackMessage = fallbackMessage;
		this.durationMs = durationMs == null ? 0L : durationMs;
	}

	public static ChatbotLog success(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		String responseMessage,
		Long durationMs
	) {
		return new ChatbotLog(
			userId,
			chatSessionId,
			intent,
			keyword,
			requestMessage,
			responseMessage,
			ChatbotLogStatus.SUCCESS,
			null,
			null,
			null,
			durationMs
		);
	}

	public static ChatbotLog failure(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		String failureReason,
		ChatbotFailureType failureType,
		String fallbackMessage,
		Long durationMs
	) {
		return new ChatbotLog(
			userId,
			chatSessionId,
			intent,
			keyword,
			requestMessage,
			fallbackMessage,
			ChatbotLogStatus.FAILURE,
			failureReason,
			failureType,
			fallbackMessage,
			durationMs
		);
	}
}