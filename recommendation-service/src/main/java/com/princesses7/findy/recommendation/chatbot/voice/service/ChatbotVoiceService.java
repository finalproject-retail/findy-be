package com.princesses7.findy.recommendation.chatbot.voice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.service.ChatbotService;
import com.princesses7.findy.recommendation.chatbot.voice.dto.response.ChatbotVoiceMessageResponse;
import com.princesses7.findy.recommendation.chatbot.voice.dto.response.SttResponse;
import com.princesses7.findy.recommendation.chatbot.voice.external.OpenAiSttClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotVoiceService {

	private final OpenAiSttClient openAiSttClient;
	private final AudioFileValidator audioFileValidator;
	private final ChatbotService chatbotService;

	@Transactional(readOnly = true)
	public SttResponse transcribe(Long userId, MultipartFile file) {
		audioFileValidator.validate(file);

		String text = openAiSttClient.transcribe(file);

		return new SttResponse(text);
	}

	@Transactional
	public ChatbotVoiceMessageResponse replyByVoice(
		Long userId,
		MultipartFile file,
		Long sessionId,
		Long storeId,
		Integer limit
	) {
		audioFileValidator.validate(file);

		String text = openAiSttClient.transcribe(file);

		ChatbotMessageResponse chatbotResponse = chatbotService.reply(
			userId,
			new ChatbotMessageRequest(
				sessionId,
				storeId,
				limit,
				text
			)
		);

		return new ChatbotVoiceMessageResponse(
			text,
			chatbotResponse
		);
	}
}