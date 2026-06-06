package com.princesses7.findy.recommendation.chatbot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.entity.ChatMessage;
import com.princesses7.findy.recommendation.chatbot.entity.ChatSenderType;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

@Component
public class ChatbotPromptContextBuilder {

	public List<OpenAiChatMessage> build(
		String systemPrompt,
		List<ChatMessage> recentMessages,
		String shoppingContextPrompt,
		String currentMessage
	) {
		List<OpenAiChatMessage> messages = new ArrayList<>();

		messages.add(OpenAiChatMessage.system(systemPrompt));

		if (shoppingContextPrompt != null && !shoppingContextPrompt.isBlank()) {
			messages.add(OpenAiChatMessage.system(shoppingContextPrompt));
		}

		recentMessages.stream()
			.map(this::toOpenAiMessage)
			.forEach(messages::add);

		messages.add(OpenAiChatMessage.user(currentMessage));

		return messages;
	}

	private OpenAiChatMessage toOpenAiMessage(ChatMessage chatMessage) {
		if (chatMessage.getSenderType() == ChatSenderType.USER) {
			return OpenAiChatMessage.user(chatMessage.getContent());
		}

		return OpenAiChatMessage.assistant(chatMessage.getContent());
	}
}