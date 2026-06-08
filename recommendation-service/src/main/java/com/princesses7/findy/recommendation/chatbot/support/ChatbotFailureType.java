package com.princesses7.findy.recommendation.chatbot.support;

public enum ChatbotFailureType {
	TIMEOUT,
	LLM_API_ERROR,
	TOKEN_LIMIT_EXCEEDED,
	EMPTY_RESPONSE,
	INVALID_RESPONSE,
	SHOPPING_DATA_API_FAILED,
	RAG_SEARCH_FAILED,
	UNKNOWN
}