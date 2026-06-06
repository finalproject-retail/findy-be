package com.princesses7.findy.recommendation.chatbot.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;
import com.princesses7.findy.recommendation.chatbot.repository.ChatMessageRepository;
import com.princesses7.findy.recommendation.chatbot.repository.ChatSessionRepository;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;

class ChatbotServiceTest {

	private final OpenAiChatClient openAiChatClient = mock(OpenAiChatClient.class);
	private final ChatbotPromptProvider chatbotPromptProvider = new ChatbotPromptProvider();
	private final ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
	private final ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
	private final ChatbotIntentAnalyzer chatbotIntentAnalyzer = mock(ChatbotIntentAnalyzer.class);
	private final ChatbotPromptContextBuilder chatbotPromptContextBuilder = new ChatbotPromptContextBuilder();
	private final ChatbotShoppingContextService chatbotShoppingContextService = mock(
		ChatbotShoppingContextService.class);
	private final ChatbotShoppingContextPromptBuilder chatbotShoppingContextPromptBuilder =
		new ChatbotShoppingContextPromptBuilder();

	private final ChatbotService chatbotService = new ChatbotService(
		openAiChatClient,
		chatbotPromptProvider,
		chatSessionRepository,
		chatMessageRepository,
		chatbotIntentAnalyzer,
		chatbotPromptContextBuilder,
		chatbotShoppingContextService,
		chatbotShoppingContextPromptBuilder
	);

	@Test
	@DisplayName("세션 ID가 없으면 새 세션을 생성하고 사용자 메시지와 챗봇 답변을 저장한다")
	void replyWithoutSessionId() {
		given(chatSessionRepository.save(any(ChatSession.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		given(chatbotIntentAnalyzer.analyze(anyString()))
			.willReturn(new ChatbotIntentAnalysis(
				ChatIntent.RECIPE_INGREDIENT_RECOMMENDATION,
				"카레",
				false
			));

		given(chatbotShoppingContextService.getContext(any(ChatbotMessageRequest.class),
			any(ChatbotIntentAnalysis.class)))
			.willReturn(null);

		given(chatMessageRepository.findRecentMessages(any(ChatSession.class), any(Pageable.class)))
			.willReturn(List.of());

		given(openAiChatClient.chat(anyList()))
			.willReturn("카레를 만들려면 카레가루, 감자, 당근, 양파가 필요해요.");

		ChatbotMessageResponse response = chatbotService.reply(
			1L,
			new ChatbotMessageRequest(
				null,
				null,
				null,
				"오늘 카레 만들고 싶어"
			)
		);

		assertThat(response.answer()).contains("카레");
		assertThat(response.shoppingContext()).isNull();

		verify(chatSessionRepository).save(any(ChatSession.class));
		verify(chatbotIntentAnalyzer).analyze("오늘 카레 만들고 싶어");
		verify(chatbotShoppingContextService).getContext(
			any(ChatbotMessageRequest.class),
			any(ChatbotIntentAnalysis.class)
		);
		verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
		verify(openAiChatClient).chat(anyList());
	}
}