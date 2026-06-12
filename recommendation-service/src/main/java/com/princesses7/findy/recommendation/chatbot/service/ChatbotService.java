package com.princesses7.findy.recommendation.chatbot.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatMessageHistoryResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatMessageItemResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatSessionResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;
import com.princesses7.findy.recommendation.chatbot.exception.ChatbotException;
import com.princesses7.findy.recommendation.chatbot.log.service.ChatbotLogService;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextPromptBuilder;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextService;
import com.princesses7.findy.recommendation.chatbot.repository.ChatMessageRepository;
import com.princesses7.findy.recommendation.chatbot.repository.ChatSessionRepository;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

	private static final int RECENT_MESSAGE_LIMIT = 4;

	private final OpenAiChatClient openAiChatClient;
	private final ChatbotPromptProvider chatbotPromptProvider;
	private final ChatSessionRepository chatSessionRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatbotIntentAnalyzer chatbotIntentAnalyzer;
	private final ChatbotPromptContextBuilder chatbotPromptContextBuilder;
	private final ChatbotShoppingContextService chatbotShoppingContextService;
	private final ChatbotShoppingContextPromptBuilder chatbotShoppingContextPromptBuilder;
	private final RagContextService ragContextService;
	private final RagContextPromptBuilder ragContextPromptBuilder;
	private final ChatbotLogService chatbotLogService;
	private final ChatbotFallbackMessageProvider chatbotFallbackMessageProvider;
	private final ChatbotRecipeRecommendationService chatbotRecipeRecommendationService;
	private final ChatbotRecipeRecommendationPromptBuilder chatbotRecipeRecommendationPromptBuilder;

	@Transactional
	public ChatbotMessageResponse reply(Long userId, ChatbotMessageRequest request) {
		LocalDateTime startedAt = LocalDateTime.now();
		ChatSession chatSession = null;
		ChatbotIntentAnalysis analysis = ChatbotIntentAnalysis.general();

		try {
			chatSession = findOrCreateSession(userId, request.sessionId(), request.message());
			analysis = chatbotIntentAnalyzer.analyze(request.message());
			ChatIntent intent = analysis.intent();

			ChatbotShoppingContextResponse shoppingContext = getShoppingContext(request, analysis);
			ChatbotRecipeRecommendationResponse recipeRecommendation = getRecipeRecommendation(
				userId,
				request,
				analysis
			);
			RagContextResponse ragContext = getRagContext(request.message(), analysis);

			if (recipeRecommendation != null) {
				String answer = createRecipeTemplateAnswer(recipeRecommendation);

				saveSuccessMessages(chatSession, request.message(), answer, intent);

				chatbotLogService.saveSuccessLog(
					userId,
					chatSession.getChatSessionId(),
					intent,
					analysis.keyword(),
					request.message(),
					answer,
					calculateDurationMs(startedAt)
				);

				return ChatbotMessageResponse.success(
					chatSession.getChatSessionId(),
					answer,
					shoppingContext,
					recipeRecommendation,
					ragContext
				);
			}

			String shoppingContextPrompt = chatbotShoppingContextPromptBuilder.build(shoppingContext);
			String recipeRecommendationPrompt = chatbotRecipeRecommendationPromptBuilder.build(recipeRecommendation);
			String ragContextPrompt = ragContextPromptBuilder.build(ragContext);

			List<ChatMessage> recentMessages = getRecentMessages(chatSession);
			List<OpenAiChatMessage> messages = chatbotPromptContextBuilder.build(
				chatbotPromptProvider.systemPrompt(),
				recentMessages,
				shoppingContextPrompt,
				recipeRecommendationPrompt,
				ragContextPrompt,
				request.message()
			);

			String answer = openAiChatClient.chat(messages);

			saveSuccessMessages(chatSession, request.message(), answer, intent);

			chatbotLogService.saveSuccessLog(
				userId,
				chatSession.getChatSessionId(),
				intent,
				analysis.keyword(),
				request.message(),
				answer,
				calculateDurationMs(startedAt)
			);

			return ChatbotMessageResponse.success(
				chatSession.getChatSessionId(),
				answer,
				shoppingContext,
				recipeRecommendation,
				ragContext
			);
		} catch (ChatbotException exception) {
			return handleFallback(
				userId,
				request,
				chatSession,
				analysis,
				exception.getFailureType(),
				exception,
				startedAt
			);
		} catch (BaseException exception) {
			throw exception;
		} catch (Exception exception) {
			return handleFallback(
				userId,
				request,
				chatSession,
				analysis,
				ChatbotFailureType.UNKNOWN,
				exception,
				startedAt
			);
		}
	}

	@Transactional(readOnly = true)
	public List<ChatSessionResponse> getSessions(Long userId) {
		return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
			.stream()
			.map(ChatSessionResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public ChatMessageHistoryResponse getMessages(Long userId, Long sessionId) {
		ChatSession chatSession = getOwnedSession(userId, sessionId);

		List<ChatMessageItemResponse> messages = chatMessageRepository
			.findByChatSessionOrderByCreatedAtAsc(chatSession)
			.stream()
			.map(ChatMessageItemResponse::from)
			.toList();

		return new ChatMessageHistoryResponse(chatSession.getChatSessionId(), messages);
	}

	private String createRecipeTemplateAnswer(ChatbotRecipeRecommendationResponse recipeRecommendation) {
		if (recipeRecommendation == null || recipeRecommendation.ingredients() == null
			|| recipeRecommendation.ingredients().isEmpty()) {
			return "필요한 재료를 찾지 못했어요. 요리명을 조금 더 구체적으로 입력해 주세요.";
		}

		String recipeName = recipeRecommendation.recipeName() == null
			? "요리"
			: recipeRecommendation.recipeName();

		return recipeName + "에 필요한 재료를 찾아봤어요. "
			+ "재료별 추천 상품은 아래 카드에서 확인하고 쇼핑리스트에 담을 수 있어요.";
	}

	private ChatSession findOrCreateSession(Long userId, Long sessionId, String firstMessage) {
		if (sessionId == null) {
			ChatSession chatSession = ChatSession.create(userId, firstMessage);
			return chatSessionRepository.save(chatSession);
		}

		return getOwnedSession(userId, sessionId);
	}

	private ChatSession getOwnedSession(Long userId, Long sessionId) {
		return chatSessionRepository.findByChatSessionIdAndUserId(sessionId, userId)
			.orElseThrow(() -> new BaseException(ErrorCode.CHAT_SESSION_NOT_FOUND));
	}

	private List<ChatMessage> getRecentMessages(ChatSession chatSession) {
		List<ChatMessage> recentMessages = new ArrayList<>(
			chatMessageRepository.findRecentMessages(
				chatSession,
				PageRequest.of(0, RECENT_MESSAGE_LIMIT)
			)
		);

		Collections.reverse(recentMessages);

		return recentMessages;
	}

	private ChatbotShoppingContextResponse getShoppingContext(
		ChatbotMessageRequest request,
		ChatbotIntentAnalysis analysis
	) {
		try {
			return chatbotShoppingContextService.getContext(request, analysis);
		} catch (ChatbotException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ChatbotException(
				ChatbotFailureType.SHOPPING_DATA_API_FAILED,
				"Failed to get chatbot shopping context",
				exception
			);
		}
	}

	private RagContextResponse getRagContext(
		String message,
		ChatbotIntentAnalysis analysis
	) {
		try {
			return ragContextService.getContext(message, analysis);
		} catch (ChatbotException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ChatbotException(
				ChatbotFailureType.RAG_SEARCH_FAILED,
				"Failed to get chatbot RAG context",
				exception
			);
		}
	}

	private void saveSuccessMessages(
		ChatSession chatSession,
		String userMessage,
		String answer,
		ChatIntent intent
	) {
		chatMessageRepository.save(ChatMessage.user(chatSession, userMessage, intent));
		chatMessageRepository.save(ChatMessage.assistant(chatSession, answer, intent));
		chatSession.updateLastMessage(answer);
	}

	private ChatbotMessageResponse handleFallback(
		Long userId,
		ChatbotMessageRequest request,
		ChatSession chatSession,
		ChatbotIntentAnalysis analysis,
		ChatbotFailureType failureType,
		Exception exception,
		LocalDateTime startedAt
	) {
		String fallbackMessage = chatbotFallbackMessageProvider.getMessage(failureType);

		saveFallbackMessages(
			chatSession,
			request.message(),
			fallbackMessage,
			analysis.intent()
		);

		chatbotLogService.saveFailureLog(
			userId,
			resolveChatSessionId(request, chatSession),
			analysis.intent(),
			analysis.keyword(),
			request.message(),
			failureType,
			fallbackMessage,
			exception,
			calculateDurationMs(startedAt)
		);

		return ChatbotMessageResponse.fallback(
			resolveChatSessionId(request, chatSession),
			fallbackMessage,
			failureType
		);
	}

	private void saveFallbackMessages(
		ChatSession chatSession,
		String userMessage,
		String fallbackMessage,
		ChatIntent intent
	) {
		if (chatSession == null) {
			return;
		}

		chatMessageRepository.save(ChatMessage.user(chatSession, userMessage, intent));
		chatMessageRepository.save(ChatMessage.assistant(chatSession, fallbackMessage, intent));
		chatSession.updateLastMessage(fallbackMessage);
	}

	private Long calculateDurationMs(LocalDateTime startedAt) {
		return Duration.between(startedAt, LocalDateTime.now()).toMillis();
	}

	private Long resolveChatSessionId(
		ChatbotMessageRequest request,
		ChatSession chatSession
	) {
		if (chatSession != null) {
			return chatSession.getChatSessionId();
		}

		return request.sessionId();
	}

	private ChatbotRecipeRecommendationResponse getRecipeRecommendation(
		Long userId,
		ChatbotMessageRequest request,
		ChatbotIntentAnalysis analysis
	) {
		try {
			return chatbotRecipeRecommendationService.recommend(userId, request, analysis);
		} catch (Exception exception) {
			return null;
		}
	}
}