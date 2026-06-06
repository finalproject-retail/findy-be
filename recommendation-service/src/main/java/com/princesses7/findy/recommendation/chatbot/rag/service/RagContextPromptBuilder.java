package com.princesses7.findy.recommendation.chatbot.rag.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagSearchResultResponse;

@Component
public class RagContextPromptBuilder {

	public String build(RagContextResponse ragContext) {
		if (ragContext == null || !ragContext.hasData()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		builder.append("""
			Findy RAG 문서 검색 결과:
			아래 내용은 서비스 정책, FAQ, 운영 문서 등 등록된 문서에서 검색된 정보이다.
			
			답변 지침:
			- 문서 검색 결과가 질문과 관련 있으면 문서 내용을 우선 근거로 답변한다.
			- 문서에 없는 내용은 확정적으로 말하지 않는다.
			- 문서 기반 답변임을 자연스럽게 표현한다.
			- 상품/재고/쿠폰/행사 데이터가 별도로 제공된 경우에는 쇼핑 데이터도 함께 고려한다.
			
			""");

		builder.append("검색어: ").append(ragContext.query()).append('\n');
		builder.append('\n');

		for (int index = 0; index < ragContext.results().size(); index++) {
			RagSearchResultResponse result = ragContext.results().get(index);

			builder.append("[문서 ").append(index + 1).append("]\n");
			builder.append("- 제목: ").append(result.title()).append('\n');
			builder.append("- 출처 유형: ").append(result.sourceType()).append('\n');
			builder.append("- 출처명: ").append(nullToDash(result.sourceName())).append('\n');
			builder.append("- 청크 번호: ").append(result.chunkIndex()).append('\n');
			builder.append("- 내용:\n");
			builder.append(result.content()).append('\n');
			builder.append('\n');
		}

		return builder.toString();
	}

	private String nullToDash(String value) {
		if (value == null || value.isBlank()) {
			return "-";
		}

		return value;
	}
}