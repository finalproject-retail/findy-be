package com.princesses7.findy.recommendation.chatbot.voice.external;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiSttClient {

	private static final String TRANSCRIPTIONS_URI = "/v1/audio/transcriptions";
	private static final String STT_MODEL = "gpt-4o-transcribe";

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;

	public String transcribe(MultipartFile file) {
		try {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", createFileResource(file));
			body.add("model", STT_MODEL);
			body.add("response_format", "text");
			body.add("language", "ko");

			String text = openAiRestClient.post()
				.uri(TRANSCRIPTIONS_URI)
				.header("Authorization", "Bearer " + properties.apiKey())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(String.class);

			if (text == null || text.isBlank()) {
				throw new BaseException(ErrorCode.CHATBOT_STT_FAILED);
			}

			return text.trim();
		} catch (BaseException exception) {
			throw exception;
		} catch (IOException | RestClientException exception) {
			throw new BaseException(ErrorCode.CHATBOT_STT_FAILED);
		}
	}

	private ByteArrayResource createFileResource(MultipartFile file) throws IOException {
		return new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		};
	}
}