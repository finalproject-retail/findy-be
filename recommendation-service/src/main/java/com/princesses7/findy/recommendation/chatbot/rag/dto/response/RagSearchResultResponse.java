package com.princesses7.findy.recommendation.chatbot.rag.dto.response;

public record RagSearchResultResponse(
	Long ragDocumentId,
	Long ragChunkId,
	String title,
	String sourceType,
	String sourceName,
	Integer chunkIndex,
	String content,
	int score
) {
}