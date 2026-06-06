package com.princesses7.findy.recommendation.chatbot.rag.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.chatbot.rag.dto.request.RagDocumentCreateRequest;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagDocumentResponse;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagDocumentService;
import com.princesses7.findy.recommendation.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RagDocumentController {

	private final RagDocumentService ragDocumentService;

	@PostMapping("/api/v1/chatbot/rag/documents")
	public ApiResponse<RagDocumentResponse> createDocument(
		@Valid @RequestBody RagDocumentCreateRequest request
	) {
		RagDocumentResponse response = ragDocumentService.create(request);

		return ApiResponse.ok("RAG 문서 저장에 성공했습니다.", response);
	}

	@GetMapping("/api/v1/chatbot/rag/documents")
	public ApiResponse<List<RagDocumentResponse>> getDocuments() {
		List<RagDocumentResponse> response = ragDocumentService.getDocuments();

		return ApiResponse.ok("RAG 문서 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/api/v1/chatbot/rag/documents/{ragDocumentId}")
	public ApiResponse<RagDocumentResponse> getDocument(
		@PathVariable Long ragDocumentId
	) {
		RagDocumentResponse response = ragDocumentService.getDocument(ragDocumentId);

		return ApiResponse.ok("RAG 문서 조회에 성공했습니다.", response);
	}

	@DeleteMapping("/api/v1/chatbot/rag/documents/{ragDocumentId}")
	public ApiResponse<Void> deleteDocument(
		@PathVariable Long ragDocumentId
	) {
		ragDocumentService.delete(ragDocumentId);

		return ApiResponse.ok("RAG 문서 삭제에 성공했습니다.", null);
	}
}