package com.princesses7.findy.recommendation.chatbot.log.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotFailureLogResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotFrequentQuestionResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotIntentCountResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotLogPageResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotLogResponse;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotSummaryResponse;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLogStatus;
import com.princesses7.findy.recommendation.chatbot.log.repository.ChatbotLogRepository;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotLogSummaryProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminChatbotLogService {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;
	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 50;

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

	public AdminChatbotSummaryResponse getSummary(
		LocalDate fromDate,
		LocalDate toDate
	) {
		LocalDateTime fromDateTime = toStartDateTime(fromDate);
		LocalDateTime toDateTime = toExclusiveEndDateTime(toDate);

		ChatbotLogSummaryProjection summary = chatbotLogRepository.getSummary(
			fromDateTime,
			toDateTime,
			ChatbotLogStatus.SUCCESS,
			ChatbotLogStatus.FAILURE
		);

		List<AdminChatbotIntentCountResponse> intentCounts = chatbotLogRepository
			.getIntentCounts(fromDateTime, toDateTime)
			.stream()
			.map(AdminChatbotIntentCountResponse::from)
			.toList();

		return AdminChatbotSummaryResponse.of(summary, intentCounts);
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

	public List<AdminChatbotFrequentQuestionResponse> getFrequentQuestions(
		LocalDate fromDate,
		LocalDate toDate,
		Integer limit
	) {
		return chatbotLogRepository.getFrequentQuestions(
				toStartDateTime(fromDate),
				toExclusiveEndDateTime(toDate),
				ChatbotLogStatus.SUCCESS,
				PageRequest.of(0, normalizeLimit(limit))
			)
			.stream()
			.map(AdminChatbotFrequentQuestionResponse::from)
			.toList();
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null || limit <= 0) {
			return DEFAULT_LIMIT;
		}

		return Math.min(limit, MAX_LIMIT);
	}

	public List<AdminChatbotFailureLogResponse> getFailureLogs(
		LocalDate fromDate,
		LocalDate toDate,
		Integer limit
	) {
		return chatbotLogRepository.getFailureLogs(
				toStartDateTime(fromDate),
				toExclusiveEndDateTime(toDate),
				ChatbotLogStatus.FAILURE,
				PageRequest.of(0, normalizeLimit(limit))
			)
			.stream()
			.map(AdminChatbotFailureLogResponse::from)
			.toList();
	}
}