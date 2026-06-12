package com.princesses7.findy.recommendation.chatbot.rag.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagSearchResultResponse;

@Component
public class RagContextPromptBuilder {

	private static final int MAX_CHUNK_CONTENT_LENGTH = 700;

	public String build(RagContextResponse ragContext) {
		if (ragContext == null || !ragContext.hasData()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		builder.append("""
			Findy RAG 참고 문서:
			- 아래 문서는 서비스 정책/FAQ 답변에만 참고한다.
			- 상품 추천, 재고, 행사, 쿠폰 데이터는 별도 쇼핑 데이터를 우선한다.
			
			""");

		builder.append("검색어: ").append(ragContext.query()).append('\n');
		builder.append('\n');

		for (int index = 0; index < ragContext.results().size(); index++) {
			RagSearchResultResponse result = ragContext.results().get(index);

			builder.append("[문서 ").append(index + 1).append("]\n");
			builder.append("- 제목: ").append(result.title()).append('\n');
			builder.append("- 내용: ");
			builder.append(truncate(result.content(), MAX_CHUNK_CONTENT_LENGTH)).append('\n');
			builder.append('\n');
		}

		return builder.toString();
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.isBlank()) {
			return "";
		}

		if (value.length() <= maxLength) {
			return value;
		}

		return value.substring(0, maxLength) + "...";
	}
}