package com.princesses7.findy.recommendation.chatbot.service;

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
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagContextResponse;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextPromptBuilder;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagContextService;
import com.princesses7.findy.recommendation.chatbot.repository.ChatMessageRepository;
import com.princesses7.findy.recommendation.chatbot.repository.ChatSessionRepository;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

	private static final int RECENT_MESSAGE_LIMIT = 8;

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

	@Transactional
	public ChatbotMessageResponse reply(Long userId, ChatbotMessageRequest request) {
		ChatSession chatSession = findOrCreateSession(userId, request.sessionId(), request.message());
		ChatbotIntentAnalysis analysis = chatbotIntentAnalyzer.analyze(request.message());
		ChatIntent intent = analysis.intent();

		ChatbotShoppingContextResponse shoppingContext = chatbotShoppingContextService.getContext(
			request,
			analysis
		);

		RagContextResponse ragContext = ragContextService.getContext(
			request.message(),
			analysis
		);

		String shoppingContextPrompt = chatbotShoppingContextPromptBuilder.build(shoppingContext);
		String ragContextPrompt = ragContextPromptBuilder.build(ragContext);

		List<ChatMessage> recentMessages = getRecentMessages(chatSession);
		List<OpenAiChatMessage> messages = chatbotPromptContextBuilder.build(
			chatbotPromptProvider.systemPrompt(),
			recentMessages,
			shoppingContextPrompt,
			ragContextPrompt,
			request.message()
		);

		String answer = openAiChatClient.chat(messages);

		chatMessageRepository.save(ChatMessage.user(chatSession, request.message(), intent));
		chatMessageRepository.save(ChatMessage.assistant(chatSession, answer, intent));
		chatSession.updateLastMessage(answer);

		return new ChatbotMessageResponse(
			chatSession.getChatSessionId(),
			answer,
			shoppingContext,
			ragContext
		);
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
}