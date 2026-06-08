package com.princesses7.findy.recommendation.chatbot.exception;

import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;

import lombok.Getter;

@Getter
public class ChatbotException extends RuntimeException {

	private final ChatbotFailureType failureType;

	public ChatbotException(ChatbotFailureType failureType, String message) {
		super(message);
		this.failureType = failureType;
	}

	public ChatbotException(ChatbotFailureType failureType, String message, Throwable cause) {
		super(message, cause);
		this.failureType = failureType;
	}
}