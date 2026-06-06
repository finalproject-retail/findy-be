package com.princesses7.findy.recommendation.external.openai;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiChatResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient {

	private static final String CHAT_COMPLETIONS_URI = "/v1/chat/completions";

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;

	public String chat(List<OpenAiChatMessage> messages) {
		return request(OpenAiChatRequest.plain(
			properties.chatModel(),
			messages
		));
	}

	public String jsonChat(List<OpenAiChatMessage> messages) {
		return request(OpenAiChatRequest.json(
			properties.chatModel(),
			messages
		));
	}

	private String request(OpenAiChatRequest request) {
		try {
			OpenAiChatResponse response = openAiRestClient.post()
				.uri(CHAT_COMPLETIONS_URI)
				.header("Authorization", "Bearer " + properties.apiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(OpenAiChatResponse.class);

			if (response == null || response.firstContent().isBlank()) {
				throw new BaseException(ErrorCode.CHATBOT_RESPONSE_FAILED);
			}

			return response.firstContent();
		} catch (BaseException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BaseException(ErrorCode.CHATBOT_RESPONSE_FAILED);
		}
	}
}