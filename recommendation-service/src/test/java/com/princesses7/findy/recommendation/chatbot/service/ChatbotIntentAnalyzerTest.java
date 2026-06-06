package com.princesses7.findy.recommendation.chatbot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;

import tools.jackson.databind.ObjectMapper;

class ChatbotIntentAnalyzerTest {

	private final OpenAiChatClient openAiChatClient = mock(OpenAiChatClient.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ChatbotIntentAnalyzer chatbotIntentAnalyzer = new ChatbotIntentAnalyzer(
		openAiChatClient,
		objectMapper
	);

	@Test
	@DisplayName("LLM 응답을 기반으로 챗봇 의도와 키워드를 분석한다")
	void analyzeIntentAndKeyword() {
		given(openAiChatClient.jsonChat(anyList()))
			.willReturn("""
				{
				  "intent": "INVENTORY_INQUIRY",
				  "keyword": "사리곰탕",
				  "shoppingDataRequired": true
				}
				""");

		ChatbotIntentAnalysis analysis = chatbotIntentAnalyzer.analyze("사리곰탕 재고 남았어?");

		assertThat(analysis.intent()).isEqualTo(ChatIntent.INVENTORY_INQUIRY);
		assertThat(analysis.keyword()).isEqualTo("사리곰탕");
		assertThat(analysis.shoppingDataRequired()).isTrue();
	}

	@Test
	@DisplayName("LLM 응답 파싱에 실패하면 기본 의도로 처리한다")
	void analyzeGeneralWhenParsingFailed() {
		given(openAiChatClient.jsonChat(anyList()))
			.willReturn("잘못된 응답");

		ChatbotIntentAnalysis analysis = chatbotIntentAnalyzer.analyze("안녕");

		assertThat(analysis.intent()).isEqualTo(ChatIntent.GENERAL);
		assertThat(analysis.keyword()).isNull();
		assertThat(analysis.shoppingDataRequired()).isFalse();
	}
}