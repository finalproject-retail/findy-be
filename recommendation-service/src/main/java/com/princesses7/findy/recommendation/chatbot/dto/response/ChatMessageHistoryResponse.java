package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.util.List;

public record ChatMessageHistoryResponse(
	Long chatSessionId,
	List<ChatMessageItemResponse> messages
) {
}