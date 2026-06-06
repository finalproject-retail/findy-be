package com.princesses7.findy.recommendation.chatbot.log.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotLogPageResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotLogResponse;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLogStatus;
import com.princesses7.findy.recommendation.chatbot.log.repository.ChatbotLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChatbotLogService {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;

	private final ChatbotLogRepository chatbotLogRepository;

	public AdminChatbotLogPageResponse getLogs(
		ChatbotLogStatus status,
		ChatIntent intent,
		LocalDate fromDate,
		LocalDate toDate,
		Integer page,
		Integer size
	) {
		PageRequest pageRequest = PageRequest.of(
			normalizePage(page),
			normalizeSize(size)
		);

		Page<ChatbotLog> logs = chatbotLogRepository.searchLogs(
			status,
			intent,
			toStartDateTime(fromDate),
			toExclusiveEndDateTime(toDate),
			pageRequest
		);

		return new AdminChatbotLogPageResponse(
			logs.getContent()
				.stream()
				.map(AdminChatbotLogResponse::from)
				.toList(),
			logs.getNumber(),
			logs.getSize(),
			logs.getTotalElements(),
			logs.getTotalPages()
		);
	}

	private int normalizePage(Integer page) {
		if (page == null || page < 0) {
			return DEFAULT_PAGE;
		}

		return page;
	}

	private int normalizeSize(Integer size) {
		if (size == null || size <= 0) {
			return DEFAULT_SIZE;
		}

		return Math.min(size, MAX_SIZE);
	}

	private LocalDateTime toStartDateTime(LocalDate date) {
		if (date == null) {
			return null;
		}

		return date.atStartOfDay();
	}

	private LocalDateTime toExclusiveEndDateTime(LocalDate date) {
		if (date == null) {
			return null;
		}

		return date.plusDays(1).atStartOfDay();
	}
}