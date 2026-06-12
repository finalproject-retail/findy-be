package com.princesses7.findy.recommendation.chatbot.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagContextService {

	private static final int DEFAULT_RAG_LIMIT = 1;
	private static final int MAX_RAG_QUERY_LENGTH = 80;

	private final RagSearchRepository ragSearchRepository;

	public RagContextResponse getContext(
		String message,
		ChatbotIntentAnalysis analysis
	) {
		if (!isRagRequired(analysis)) {
			return RagContextResponse.empty(message);
		}

		String query = resolveQuery(message, analysis);

		if (query == null || query.isBlank()) {
			return RagContextResponse.empty(message);
		}

		query = truncate(query, MAX_RAG_QUERY_LENGTH);

		return new RagContextResponse(
			query,
			ragSearchRepository.search(query, DEFAULT_RAG_LIMIT)
		);
	}

	private boolean isRagRequired(ChatbotIntentAnalysis analysis) {
		if (analysis == null || analysis.intent() == null) {
			return true;
		}

		ChatIntent intent = analysis.intent();

		return intent == ChatIntent.GENERAL;
	}

	private String resolveQuery(
		String message,
		ChatbotIntentAnalysis analysis
	) {
		if (analysis != null
			&& analysis.keyword() != null
			&& !analysis.keyword().isBlank()) {
			return analysis.keyword();
		}

		return message;
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength);
	}
}