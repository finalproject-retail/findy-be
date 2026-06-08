package com.princesses7.findy.recommendation.external.openai;

import java.net.SocketTimeoutException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.princesses7.findy.recommendation.chatbot.exception.ChatbotException;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotFailureType;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiChatResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient {

	private static final String CHAT_COMPLETIONS_URI = "/v1/chat/completions";
	private static final int MAX_TOTAL_MESSAGE_LENGTH = 1_000;

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
		validateTokenLimit(request);

		try {
			OpenAiChatResponse response = openAiRestClient.post()
				.uri(CHAT_COMPLETIONS_URI)
				.header("Authorization", "Bearer " + properties.apiKey())
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(OpenAiChatResponse.class);

			String content = response == null ? null : response.firstContent();

			if (content == null || content.isBlank()) {
				throw new ChatbotException(
					ChatbotFailureType.EMPTY_RESPONSE,
					"LLM response content is empty"
				);
			}

			return content;
		} catch (ChatbotException exception) {
			throw exception;
		} catch (RestClientResponseException exception) {
			if (isTokenLimitExceeded(exception)) {
				throw new ChatbotException(
					ChatbotFailureType.TOKEN_LIMIT_EXCEEDED,
					"LLM token limit exceeded",
					exception
				);
			}

			throw new ChatbotException(
				ChatbotFailureType.LLM_API_ERROR,
				"LLM API request failed. status=" + exception.getStatusCode(),
				exception
			);
		} catch (ResourceAccessException exception) {
			if (isTimeout(exception)) {
				throw new ChatbotException(
					ChatbotFailureType.TIMEOUT,
					"LLM request timeout",
					exception
				);
			}

			throw new ChatbotException(
				ChatbotFailureType.LLM_API_ERROR,
				"LLM API request failed",
				exception
			);
		} catch (Exception exception) {
			if (isTimeout(exception)) {
				throw new ChatbotException(
					ChatbotFailureType.TIMEOUT,
					"LLM request timeout",
					exception
				);
			}

			throw new ChatbotException(
				ChatbotFailureType.UNKNOWN,
				"Unexpected LLM error",
				exception
			);
		}
	}

	private void validateTokenLimit(OpenAiChatRequest request) {
		int totalLength = request.messages() == null ? 0 : request.messages()
														   .stream()
														   .map(OpenAiChatMessage::content)
														   .filter(content -> content != null)
														   .mapToInt(String::length)
														   .sum();

		if (totalLength > MAX_TOTAL_MESSAGE_LENGTH) {
			throw new ChatbotException(
				ChatbotFailureType.TOKEN_LIMIT_EXCEEDED,
				"LLM prompt length exceeded. length=" + totalLength
			);
		}
	}

	private boolean isTokenLimitExceeded(RestClientResponseException exception) {
		String responseBody = exception.getResponseBodyAsString();
		String message = (exception.getMessage() + " " + responseBody).toLowerCase();

		return message.contains("token")
			|| message.contains("context length")
			|| message.contains("maximum context")
			|| message.contains("too large");
	}

	private boolean isTimeout(Throwable throwable) {
		Throwable current = throwable;

		while (current != null) {
			if (current instanceof SocketTimeoutException) {
				return true;
			}

			String className = current.getClass().getSimpleName().toLowerCase();
			String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase();

			if (className.contains("timeout")
				|| message.contains("timeout")
				|| message.contains("timed out")) {
				return true;
			}

			current = current.getCause();
		}

		return false;
	}
}