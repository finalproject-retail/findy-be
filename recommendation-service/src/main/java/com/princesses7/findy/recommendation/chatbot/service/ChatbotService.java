package com.princesses7.findy.recommendation.chatbot.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatMessageHistoryResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatMessageItemResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatSessionResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSession;
import com.princesses7.findy.recommendation.chatbot.repository.ChatMessageRepository;
import com.princesses7.findy.recommendation.chatbot.repository.ChatSessionRepository;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatMessage;
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
	private final ChatbotPromptContextBuilder chatbotPromptContextBuilder;

	@Transactional
	public ChatbotMessageResponse reply(Long userId, ChatbotMessageRequest request) {
		ChatSession chatSession = findOrCreateSession(userId, request.sessionId(), request.message());
		ChatIntent intent = ChatIntent.GENERAL;

		List<ChatMessage> recentMessages = getRecentMessages(chatSession);
		List<OpenAiChatMessage> messages = chatbotPromptContextBuilder.build(
			chatbotPromptProvider.systemPrompt(),
			recentMessages,
			request.message()
		);

		String answer = openAiChatClient.chat(messages);

		chatMessageRepository.save(ChatMessage.user(chatSession, request.message(), intent));
		chatMessageRepository.save(ChatMessage.assistant(chatSession, answer, intent));
		chatSession.updateLastMessage(answer);

		return new ChatbotMessageResponse(chatSession.getChatSessionId(), answer);
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