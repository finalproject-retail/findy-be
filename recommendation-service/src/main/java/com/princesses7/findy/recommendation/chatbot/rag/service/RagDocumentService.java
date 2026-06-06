package com.princesses7.findy.recommendation.chatbot.rag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.rag.dto.request.RagDocumentCreateRequest;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagChunkResponse;
import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagDocumentResponse;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagChunk;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagDocument;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagChunkRepository;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagDocumentRepository;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RagDocumentService {

	private final RagDocumentRepository ragDocumentRepository;
	private final RagChunkRepository ragChunkRepository;
	private final RagChunkSplitter ragChunkSplitter;

	@Transactional
	public RagDocumentResponse create(RagDocumentCreateRequest request) {
		List<String> chunkContents = ragChunkSplitter.split(request.content());

		if (chunkContents.isEmpty()) {
			throw new BaseException(ErrorCode.RAG_DOCUMENT_EMPTY);
		}

		RagDocument ragDocument = RagDocument.create(
			request.title(),
			request.sourceType(),
			request.sourceName(),
			request.content()
		);

		RagDocument savedDocument = ragDocumentRepository.save(ragDocument);

		List<RagChunk> chunks = saveChunks(savedDocument, chunkContents);

		return RagDocumentResponse.of(
			savedDocument,
			chunks.stream()
				.map(RagChunkResponse::from)
				.toList()
		);
	}

	@Transactional(readOnly = true)
	public List<RagDocumentResponse> getDocuments() {
		return ragDocumentRepository.findByActiveTrueOrderByUpdatedAtDesc()
			.stream()
			.map(document -> {
				List<RagChunkResponse> chunks = ragChunkRepository
					.findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(document)
					.stream()
					.map(RagChunkResponse::from)
					.toList();

				return RagDocumentResponse.of(document, chunks);
			})
			.toList();
	}

	@Transactional(readOnly = true)
	public RagDocumentResponse getDocument(Long ragDocumentId) {
		RagDocument document = getActiveDocument(ragDocumentId);

		List<RagChunkResponse> chunks = ragChunkRepository
			.findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(document)
			.stream()
			.map(RagChunkResponse::from)
			.toList();

		return RagDocumentResponse.of(document, chunks);
	}

	@Transactional
	public void delete(Long ragDocumentId) {
		RagDocument document = getActiveDocument(ragDocumentId);
		document.deactivate();

		ragChunkRepository.findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(document)
			.forEach(RagChunk::deactivate);
	}

	private RagDocument getActiveDocument(Long ragDocumentId) {
		return ragDocumentRepository.findById(ragDocumentId)
			.filter(RagDocument::isActive)
			.orElseThrow(() -> new BaseException(ErrorCode.RAG_DOCUMENT_NOT_FOUND));
	}

	private List<RagChunk> saveChunks(
		RagDocument ragDocument,
		List<String> chunkContents
	) {
		for (int index = 0; index < chunkContents.size(); index++) {
			RagChunk chunk = RagChunk.create(
				ragDocument,
				index,
				chunkContents.get(index)
			);

			ragChunkRepository.save(chunk);
		}

		return ragChunkRepository.findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(ragDocument);
	}
}