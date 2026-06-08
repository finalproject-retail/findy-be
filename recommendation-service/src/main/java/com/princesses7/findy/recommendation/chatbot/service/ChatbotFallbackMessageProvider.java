package com.princesses7.findy.recommendation.chatbot.service;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;

@Component
public class ChatbotFallbackMessageProvider {

	private static final String DEFAULT_MESSAGE = "일시적으로 챗봇 답변을 생성하지 못했어요. 잠시 후 다시 이용해 주세요.";

	private final Map<ChatbotFailureType, String> messages = new EnumMap<>(ChatbotFailureType.class);

	public ChatbotFallbackMessageProvider() {
		messages.put(ChatbotFailureType.TIMEOUT, "응답이 조금 지연되고 있어요. 잠시 후 다시 질문해 주세요.");
		messages.put(ChatbotFailureType.LLM_API_ERROR, DEFAULT_MESSAGE);
		messages.put(ChatbotFailureType.TOKEN_LIMIT_EXCEEDED, "질문 내용이 너무 길어요. 핵심만 짧게 다시 입력해 주세요.");
		messages.put(ChatbotFailureType.EMPTY_RESPONSE, "답변을 제대로 생성하지 못했어요. 다시 한 번 질문해 주세요.");
		messages.put(ChatbotFailureType.INVALID_RESPONSE, "답변을 제대로 생성하지 못했어요. 다시 한 번 질문해 주세요.");
		messages.put(ChatbotFailureType.SHOPPING_DATA_API_FAILED, "현재 쇼핑 정보를 불러오지 못했어요. 잠시 후 다시 확인해 주세요.");
		messages.put(ChatbotFailureType.RAG_SEARCH_FAILED, "관련 안내 정보를 찾는 중 문제가 발생했어요. 잠시 후 다시 질문해 주세요.");
		messages.put(ChatbotFailureType.UNKNOWN, DEFAULT_MESSAGE);
	}

	public String getMessage(ChatbotFailureType failureType) {
		if (failureType == null) {
			return DEFAULT_MESSAGE;
		}

		return messages.getOrDefault(failureType, DEFAULT_MESSAGE);
	}
}