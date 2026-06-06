package com.princesses7.findy.recommendation.chatbot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.service.ChatbotService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

	private final ChatbotService chatbotService;

	@PostMapping("/messages")
	public ApiResponse<ChatbotMessageResponse> sendMessage(
		@Valid @RequestBody ChatbotMessageRequest request
	) {
		ChatbotMessageResponse response = chatbotService.reply(request);

		return ApiResponse.ok("챗봇 답변 생성에 성공했습니다.", response);
	}
}