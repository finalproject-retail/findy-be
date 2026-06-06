package com.princesses7.findy.recommendation.chatbot.rag.dto.response;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagChunk;

public record RagChunkResponse(
	Long ragChunkId,
	Integer chunkIndex,
	String content,
	Integer tokenCount
) {

	public static RagChunkResponse from(RagChunk ragChunk) {
		return new RagChunkResponse(
			ragChunk.getRagChunkId(),
			ragChunk.getChunkIndex(),
			ragChunk.getContent(),
			ragChunk.getTokenCount()
		);
	}
}