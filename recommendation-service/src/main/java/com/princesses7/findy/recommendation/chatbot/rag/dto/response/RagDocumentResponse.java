package com.princesses7.findy.recommendation.chatbot.rag.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagDocument;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagSourceType;

public record RagDocumentResponse(
	Long ragDocumentId,
	String title,
	RagSourceType sourceType,
	String sourceName,
	boolean active,
	int chunkCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<RagChunkResponse> chunks
) {

	public static RagDocumentResponse of(
		RagDocument ragDocument,
		List<RagChunkResponse> chunks
	) {
		return new RagDocumentResponse(
			ragDocument.getRagDocumentId(),
			ragDocument.getTitle(),
			ragDocument.getSourceType(),
			ragDocument.getSourceName(),
			ragDocument.isActive(),
			chunks.size(),
			ragDocument.getCreatedAt(),
			ragDocument.getUpdatedAt(),
			chunks
		);
	}
}