package com.princesses7.findy.recommendation.chatbot.log.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.repository.ChatbotLogRepository;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		try {
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
		} catch (Exception exception) {
			log.warn("Failed to save chatbot success log. message={}", exception.getMessage());
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveFailureLog(
		Long userId,
		Long chatSessionId,
		ChatIntent intent,
		String keyword,
		String requestMessage,
		ChatbotFailureType failureType,
		String fallbackMessage,
		Exception exception,
		Long durationMs
	) {
		try {
			ChatbotLog chatbotLog = ChatbotLog.failure(
				userId,
				chatSessionId,
				intent,
				keyword,
				requestMessage,
				resolveFailureReason(exception),
				failureType,
				fallbackMessage,
				durationMs
			);

			chatbotLogRepository.save(chatbotLog);
		} catch (Exception logException) {
			log.warn("Failed to save chatbot failure log. message={}", logException.getMessage());
		}
	}

	private String resolveFailureReason(Exception exception) {
		if (exception == null) {
			return null;
		}

		String message = exception.getMessage();

		if (message == null || message.isBlank()) {
			message = exception.getClass().getSimpleName();
		} else {
			message = exception.getClass().getSimpleName() + ": " + message;
		}

		if (message.length() <= MAX_FAILURE_REASON_LENGTH) {
			return message;
		}

		return message.substring(0, MAX_FAILURE_REASON_LENGTH);
	}
}