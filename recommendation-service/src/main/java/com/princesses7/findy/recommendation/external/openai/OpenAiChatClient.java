package com.princesses7.findy.recommendation.external.openai;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.OpenAiChatResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient {

	private static final String CHAT_COMPLETIONS_URI = "/v1/chat/completions";

	private final RestClient openAiRestClient;
	private final OpenAiProperties openAiProperties;

	public String chat(List<OpenAiChatMessage> messages) {
		OpenAiChatRequest request = new OpenAiChatRequest(
			openAiProperties.chatModel(),
			messages,
			0.2,
			700
		);

		try {
			OpenAiChatResponse response = openAiRestClient.post()
				.uri(CHAT_COMPLETIONS_URI)
				.header("Authorization", "Bearer " + openAiProperties.apiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(OpenAiChatResponse.class);

			if (response == null) {
				throw new BaseException(ErrorCode.CHATBOT_RESPONSE_FAILED);
			}

			return response.firstAnswer();
		} catch (BaseException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BaseException(ErrorCode.CHATBOT_RESPONSE_FAILED);
		}
	}
}