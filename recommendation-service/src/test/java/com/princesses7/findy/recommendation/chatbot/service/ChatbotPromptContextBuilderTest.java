package com.princesses7.findy.recommendation.chatbot.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

class ChatbotPromptContextBuilderTest {

	private final ChatbotPromptContextBuilder chatbotPromptContextBuilder = new ChatbotPromptContextBuilder();

	@Test
	@DisplayName("시스템 프롬프트, 쇼핑 데이터, RAG 데이터, 최근 대화, 현재 메시지를 순서대로 조합한다")
	void buildPromptWithShoppingContextRagContextAndRecentContext() {
		List<ChatMessage> recentMessages = List.of(
			ChatMessage.user(null, "사리곰탕 있어?", ChatIntent.INVENTORY_INQUIRY),
			ChatMessage.assistant(null, "사리곰탕 상품을 확인해드릴게요.", ChatIntent.INVENTORY_INQUIRY)
		);

		List<OpenAiChatMessage> messages = chatbotPromptContextBuilder.build(
			"너는 Findy AI 쇼핑 도우미이다.",
			recentMessages,
			"Findy 쇼핑 데이터 조회 결과: 사리곰탕 재고 3개",
			"Findy RAG 문서 검색 결과: 쿠폰은 주문 화면에서 사용할 수 있습니다.",
			"쿠폰도 있어?"
		);

		assertThat(messages).hasSize(6);
		assertThat(messages.get(0).role()).isEqualTo("system");
		assertThat(messages.get(1).role()).isEqualTo("system");
		assertThat(messages.get(2).role()).isEqualTo("system");
		assertThat(messages.get(3).role()).isEqualTo("user");
		assertThat(messages.get(4).role()).isEqualTo("assistant");
		assertThat(messages.get(5).role()).isEqualTo("user");
		assertThat(messages.get(2).content()).contains("쿠폰은 주문 화면");
	}
}