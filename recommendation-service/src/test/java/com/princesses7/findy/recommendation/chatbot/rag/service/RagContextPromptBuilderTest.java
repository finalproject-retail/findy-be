package com.princesses7.findy.recommendation.chatbot.rag.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagSearchResultResponse;

class RagContextPromptBuilderTest {

	private final RagContextPromptBuilder ragContextPromptBuilder = new RagContextPromptBuilder();

	@Test
	@DisplayName("RAG 검색 결과를 프롬프트로 구성한다")
	void buildRagContextPrompt() {
		RagContextResponse ragContext = new RagContextResponse(
			"쿠폰 사용 방법",
			List.of(
				new RagSearchResultResponse(
					1L,
					1L,
					"쿠폰 이용 안내",
					"FAQ",
					"고객 FAQ",
					0,
					"쿠폰은 주문 화면에서 적용할 수 있습니다.",
					100
				)
			)
		);

		String prompt = ragContextPromptBuilder.build(ragContext);

		assertThat(prompt).contains("Findy RAG 문서 검색 결과");
		assertThat(prompt).contains("쿠폰 이용 안내");
		assertThat(prompt).contains("쿠폰은 주문 화면에서 적용할 수 있습니다.");
	}

	@Test
	@DisplayName("RAG 검색 결과가 없으면 빈 프롬프트를 반환한다")
	void buildEmptyRagContextPrompt() {
		RagContextResponse ragContext = RagContextResponse.empty("없는 질문");

		String prompt = ragContextPromptBuilder.build(ragContext);

		assertThat(prompt).isBlank();
	}
}