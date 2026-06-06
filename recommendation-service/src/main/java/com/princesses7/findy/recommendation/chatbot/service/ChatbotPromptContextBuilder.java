package com.princesses7.findy.recommendation.chatbot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSenderType;
import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatMessage;

@Component
public class ChatbotPromptContextBuilder {

	public List<OpenAiChatMessage> build(
		String systemPrompt,
		List<ChatMessage> recentMessages,
		String currentMessage
	) {
		List<OpenAiChatMessage> messages = new ArrayList<>();

		messages.add(new OpenAiChatMessage("system", systemPrompt));

		recentMessages.stream()
			.map(this::toOpenAiMessage)
			.forEach(messages::add);

		messages.add(new OpenAiChatMessage("user", currentMessage));

		return messages;
	}

	private OpenAiChatMessage toOpenAiMessage(ChatMessage chatMessage) {
		if (chatMessage.getSenderType() == ChatSenderType.USER) {
			return new OpenAiChatMessage("user", chatMessage.getContent());
		}

		return new OpenAiChatMessage("assistant", chatMessage.getContent());
	}
}