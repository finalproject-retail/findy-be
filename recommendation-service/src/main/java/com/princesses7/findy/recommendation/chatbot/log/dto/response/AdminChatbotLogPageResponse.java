package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import java.util.List;

public record AdminChatbotLogPageResponse(
	List<AdminChatbotLogResponse> logs,
	int page,
	int size,
	long totalElements,
	int totalPages
) {
}