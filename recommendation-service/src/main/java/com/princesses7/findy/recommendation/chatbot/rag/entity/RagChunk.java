package com.princesses7.findy.recommendation.chatbot.rag.entity;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chatbot_rag_chunks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagChunk extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ragChunkId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rag_document_id", nullable = false)
	private RagDocument ragDocument;

	@Column(name = "chunk_index", nullable = false)
	private Integer chunkIndex;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "token_count", nullable = false)
	private Integer tokenCount;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	private RagChunk(
		RagDocument ragDocument,
		Integer chunkIndex,
		String content,
		Integer tokenCount
	) {
		this.ragDocument = ragDocument;
		this.chunkIndex = chunkIndex;
		this.content = content;
		this.tokenCount = tokenCount;
		this.active = true;
	}

	public static RagChunk create(
		RagDocument ragDocument,
		Integer chunkIndex,
		String content
	) {
		return new RagChunk(
			ragDocument,
			chunkIndex,
			content,
			estimateTokenCount(content)
		);
	}

	public void deactivate() {
		this.active = false;
	}

	private static int estimateTokenCount(String content) {
		if (content == null || content.isBlank()) {
			return 0;
		}

		return Math.max(1, content.length() / 3);
	}
}