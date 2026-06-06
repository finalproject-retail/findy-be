package com.princesses7.findy.recommendation.chatbot.rag.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagSearchRepository;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot/rag/search")
public class RagSearchController {

	private static final int DEFAULT_LIMIT = 5;

	private final RagSearchRepository ragSearchRepository;

	@GetMapping
	public ApiResponse<RagContextResponse> search(
		@RequestParam String query,
		@RequestParam(required = false) Integer limit
	) {
		int normalizedLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : limit;

		RagContextResponse response = new RagContextResponse(
			query,
			ragSearchRepository.search(query, normalizedLimit)
		);

		return ApiResponse.ok("RAG 문서 검색에 성공했습니다.", response);
	}
}