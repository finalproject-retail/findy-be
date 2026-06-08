package com.princesses7.findy.recommendation.chatbot.voice.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.princesses7.findy.recommendation.chatbot.voice.dto.response.ChatbotVoiceMessageResponse;
import com.princesses7.findy.recommendation.chatbot.voice.dto.response.SttResponse;
import com.princesses7.findy.recommendation.chatbot.voice.service.ChatbotVoiceService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot")
public class ChatbotVoiceController {

	private final ChatbotVoiceService chatbotVoiceService;

	@PostMapping("/stt")
	public ApiResponse<SttResponse> transcribe(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam("file") MultipartFile file
	) {
		return ApiResponse.ok(
			"음성 인식에 성공했습니다.",
			chatbotVoiceService.transcribe(userId, file)
		);
	}

	@PostMapping("/messages/voice")
	public ApiResponse<ChatbotVoiceMessageResponse> replyByVoice(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam("file") MultipartFile file,
		@RequestParam(required = false) Long sessionId,
		@RequestParam(required = false) Long storeId,
		@RequestParam(required = false) Integer limit
	) {
		return ApiResponse.ok(
			"음성 챗봇 답변 생성에 성공했습니다.",
			chatbotVoiceService.replyByVoice(
				userId,
				file,
				sessionId,
				storeId,
				limit
			)
		);
	}
}