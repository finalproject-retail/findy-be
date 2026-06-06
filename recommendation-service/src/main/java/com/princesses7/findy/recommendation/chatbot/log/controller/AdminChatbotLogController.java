package com.princesses7.findy.recommendation.chatbot.log.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.dto.response.AdminChatbotLogPageResponse;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLogStatus;
import com.princesses7.findy.recommendation.chatbot.log.service.AdminChatbotLogService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/chatbot/logs")
public class AdminChatbotLogController {

	private final AdminChatbotLogService adminChatbotLogService;

	@GetMapping
	public ApiResponse<AdminChatbotLogPageResponse> getLogs(
		@RequestParam(required = false) ChatbotLogStatus status,
		@RequestParam(required = false) ChatIntent intent,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size
	) {
		AdminChatbotLogPageResponse response = adminChatbotLogService.getLogs(
			status,
			intent,
			fromDate,
			toDate,
			page,
			size
		);

		return ApiResponse.ok("관리자 챗봇 로그 조회에 성공했습니다.", response);
	}
}