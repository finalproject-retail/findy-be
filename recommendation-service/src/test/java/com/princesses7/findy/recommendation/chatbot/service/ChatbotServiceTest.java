package com.princesses7.findy.recommendation.chatbot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;

class ChatbotServiceTest {

	private final OpenAiChatClient openAiChatClient = mock(OpenAiChatClient.class);
	private final ChatbotPromptProvider chatbotPromptProvider = new ChatbotPromptProvider();
	private final ChatbotService chatbotService = new ChatbotService(
		openAiChatClient,
		chatbotPromptProvider
	);

	@Test
	@DisplayName("사용자 메시지를 기반으로 챗봇 답변을 생성한다")
	void reply() {
		given(openAiChatClient.chat(ArgumentMatchers.anyList()))
			.willReturn("카레를 만들려면 카레가루, 감자, 당근, 양파가 필요해요.");

		ChatbotMessageResponse response = chatbotService.reply(
			new ChatbotMessageRequest("오늘 카레 만들고 싶어")
		);

		assertThat(response.answer()).contains("카레");
	}
}