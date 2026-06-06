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
import com.princesses7.findy.recommendation.chatbot.log.service.ChatbotLogService;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextPromptBuilder;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextService;
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
	private final RagContextService ragContextService = mock(RagContextService.class);
	private final RagContextPromptBuilder ragContextPromptBuilder = new RagContextPromptBuilder();
	private final ChatbotLogService chatbotLogService = mock(ChatbotLogService.class);

	private final ChatbotService chatbotService = new ChatbotService(
		openAiChatClient,
		chatbotPromptProvider,
		chatSessionRepository,
		chatMessageRepository,
		chatbotIntentAnalyzer,
		chatbotPromptContextBuilder,
		chatbotShoppingContextService,
		chatbotShoppingContextPromptBuilder,
		ragContextService,
		ragContextPromptBuilder,
		chatbotLogService
	);

	@Test
	@DisplayName("챗봇 답변 생성 성공 시 사용자 메시지, 챗봇 답변, 성공 로그를 저장한다")
	void replySuccessWithLog() {
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

		given(ragContextService.getContext(anyString(), any(ChatbotIntentAnalysis.class)))
			.willReturn(RagContextResponse.empty("오늘 카레 만들고 싶어"));

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
		assertThat(response.ragContext()).isNotNull();

		verify(chatSessionRepository).save(any(ChatSession.class));
		verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
		verify(chatbotLogService).saveSuccessLog(
			eq(1L),
			any(),
			eq(ChatIntent.RECIPE_INGREDIENT_RECOMMENDATION),
			eq("카레"),
			eq("오늘 카레 만들고 싶어"),
			contains("카레"),
			anyLong()
		);
	}

	@Test
	@DisplayName("챗봇 답변 생성 실패 시 실패 로그를 저장하고 예외를 전파한다")
	void replyFailureWithLog() {
		given(chatSessionRepository.save(any(ChatSession.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		given(chatbotIntentAnalyzer.analyze(anyString()))
			.willReturn(new ChatbotIntentAnalysis(
				ChatIntent.COUPON_INQUIRY,
				"쿠폰",
				true
			));

		given(chatbotShoppingContextService.getContext(any(ChatbotMessageRequest.class),
			any(ChatbotIntentAnalysis.class)))
			.willReturn(null);

		given(ragContextService.getContext(anyString(), any(ChatbotIntentAnalysis.class)))
			.willReturn(RagContextResponse.empty("쿠폰은 어디서 써?"));

		given(chatMessageRepository.findRecentMessages(any(ChatSession.class), any(Pageable.class)))
			.willReturn(List.of());

		given(openAiChatClient.chat(anyList()))
			.willThrow(new RuntimeException("OpenAI 호출 실패"));

		assertThatThrownBy(() -> chatbotService.reply(
			1L,
			new ChatbotMessageRequest(
				null,
				null,
				null,
				"쿠폰은 어디서 써?"
			)
		)).isInstanceOf(RuntimeException.class);

		verify(chatbotLogService).saveFailureLog(
			eq(1L),
			any(),
			eq(ChatIntent.COUPON_INQUIRY),
			eq("쿠폰"),
			eq("쿠폰은 어디서 써?"),
			any(Exception.class),
			anyLong()
		);
	}
}