package com.princesses7.findy.recommendation.chatbot.rag.dto.request;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagSourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagDocumentCreateRequest(

	@NotBlank(message = "문서 제목을 입력해 주세요.")
	@Size(max = 255, message = "문서 제목은 255자 이하로 입력해 주세요.")
	String title,

	RagSourceType sourceType,

	@Size(max = 255, message = "문서 출처명은 255자 이하로 입력해 주세요.")
	String sourceName,

	@NotBlank(message = "문서 내용을 입력해 주세요.")
	String content
) {
}