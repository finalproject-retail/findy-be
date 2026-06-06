package com.princesses7.findy.recommendation.chatbot.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagContextService {

	private static final int DEFAULT_RAG_LIMIT = 3;

	private final RagSearchRepository ragSearchRepository;

	public RagContextResponse getContext(
		String message,
		ChatbotIntentAnalysis analysis
	) {
		String query = resolveQuery(message, analysis);

		if (query == null || query.isBlank()) {
			return RagContextResponse.empty(message);
		}

		return new RagContextResponse(
			query,
			ragSearchRepository.search(query, DEFAULT_RAG_LIMIT)
		);
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
}