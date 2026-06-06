package com.princesses7.findy.recommendation.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

	private final OpenAiChatClient openAiChatClient;
	private final ChatbotPromptProvider chatbotPromptProvider;

	public ChatbotMessageResponse reply(ChatbotMessageRequest request) {
		List<OpenAiChatMessage> messages = List.of(
			new OpenAiChatMessage("system", chatbotPromptProvider.systemPrompt()),
			new OpenAiChatMessage("user", request.message())
		);

		String answer = openAiChatClient.chat(messages);

		return new ChatbotMessageResponse(answer);
	}
}