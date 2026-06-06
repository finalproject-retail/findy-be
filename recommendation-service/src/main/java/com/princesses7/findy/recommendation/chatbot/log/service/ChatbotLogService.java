package com.princesses7.findy.recommendation.chatbot.log.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.repository.ChatbotLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotLogService {

	private static final int MAX_FAILURE_REASON_LENGTH = 1000;

	private final ChatbotLogRepository chatbotLogRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveSuccessLog(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		String responseMessage,
		Long durationMs
	) {
		ChatbotLog chatbotLog = ChatbotLog.success(
			userId,
			chatSessionId,
			intent,
			keyword,
			requestMessage,
			responseMessage,
			durationMs
		);

		chatbotLogRepository.save(chatbotLog);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveFailureLog(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		Exception exception,
		Long durationMs
	) {
		ChatbotLog chatbotLog = ChatbotLog.failure(
			userId,
			chatSessionId,
			intent,
			keyword,
			requestMessage,
			resolveFailureReason(exception),
			durationMs
		);

		chatbotLogRepository.save(chatbotLog);
	}

	private String resolveFailureReason(Exception exception) {
		if (exception == null) {
			return null;
		}

		String message = exception.getMessage();

		if (message == null || message.isBlank()) {
			message = exception.getClass().getSimpleName();
		}

		if (message.length() <= MAX_FAILURE_REASON_LENGTH) {
			return message;
		}

		return message.substring(0, MAX_FAILURE_REASON_LENGTH);
	}
}