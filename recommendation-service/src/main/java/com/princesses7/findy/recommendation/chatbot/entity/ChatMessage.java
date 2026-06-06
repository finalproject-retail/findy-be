package com.princesses7.findy.recommendation.chatbot.entity;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long chatMessageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_session_id", nullable = false)
	private ChatSession chatSession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ChatSenderType senderType;

	@Lob
	@Column(nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private ChatIntent intent;

	private ChatMessage(
		ChatSession chatSession,
		ChatSenderType senderType,
		String content,
		ChatIntent intent
	) {
		this.chatSession = chatSession;
		this.senderType = senderType;
		this.content = content;
		this.intent = intent;
	}

	public static ChatMessage user(ChatSession chatSession, String content, ChatIntent intent) {
		return new ChatMessage(chatSession, ChatSenderType.USER, content, intent);
	}

	public static ChatMessage assistant(ChatSession chatSession, String content, ChatIntent intent) {
		return new ChatMessage(chatSession, ChatSenderType.ASSISTANT, content, intent);
	}
}