package com.princesses7.findy.recommendation.chatbot.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatMessageHistoryResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatSessionResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.service.ChatbotService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

	private static final String USER_ID_HEADER = "X-User-Id";

	private final ChatbotService chatbotService;

	@PostMapping("/messages")
	public ApiResponse<ChatbotMessageResponse> sendMessage(
		@RequestHeader(USER_ID_HEADER) Long userId,
		@Valid @RequestBody ChatbotMessageRequest request
	) {
		ChatbotMessageResponse response = chatbotService.reply(userId, request);

		return ApiResponse.ok("챗봇 답변 생성에 성공했습니다.", response);
	}

	@GetMapping("/sessions")
	public ApiResponse<List<ChatSessionResponse>> getSessions(
		@RequestHeader(USER_ID_HEADER) Long userId
	) {
		List<ChatSessionResponse> response = chatbotService.getSessions(userId);

		return ApiResponse.ok("채팅 세션 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/sessions/{sessionId}/messages")
	public ApiResponse<ChatMessageHistoryResponse> getMessages(
		@RequestHeader(USER_ID_HEADER) Long userId,
		@PathVariable Long sessionId
	) {
		ChatMessageHistoryResponse response = chatbotService.getMessages(userId, sessionId);

		return ApiResponse.ok("채팅 메시지 목록 조회에 성공했습니다.", response);
	}
}