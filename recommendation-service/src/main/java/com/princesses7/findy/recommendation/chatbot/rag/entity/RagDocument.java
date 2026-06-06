package com.princesses7.findy.recommendation.chatbot.rag.entity;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chatbot_rag_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RagDocument extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ragDocumentId;

	@Column(nullable = false)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 50)
	private RagSourceType sourceType;

	@Column(name = "source_name")
	private String sourceName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	private RagDocument(
		String title,
		RagSourceType sourceType,
		String sourceName,
		String content
	) {
		this.title = title;
		this.sourceType = sourceType;
		this.sourceName = sourceName;
		this.content = content;
		this.active = true;
	}

	public static RagDocument create(
		String title,
		RagSourceType sourceType,
		String sourceName,
		String content
	) {
		return new RagDocument(
			title,
			sourceType == null ? RagSourceType.ETC : sourceType,
			sourceName,
			content
		);
	}

	public void deactivate() {
		this.active = false;
	}
}