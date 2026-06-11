package com.princesses7.findy.recommendation.chatbot.rag.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.rag.entity.RagChunk;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagDocument;
import com.princesses7.findy.recommendation.chatbot.rag.entity.RagSourceType;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagChunkRepository;
import com.princesses7.findy.recommendation.chatbot.rag.repository.RagDocumentRepository;
import com.princesses7.findy.recommendation.chatbot.rag.service.RagChunkSplitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagResourceImportRunner implements ApplicationRunner {

	private static final String RAG_RESOURCE_PATTERN = "classpath*:rag/*.md";

	private final RagDocumentRepository ragDocumentRepository;
	private final RagChunkRepository ragChunkRepository;
	private final RagChunkSplitter ragChunkSplitter;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		try {
			Resource[] resources = resolver.getResources(RAG_RESOURCE_PATTERN);

			if (resources.length == 0) {
				log.info("RAG 리소스 문서가 없습니다. pattern={}", RAG_RESOURCE_PATTERN);
				return;
			}

			int importedCount = 0;
			int skippedCount = 0;

			for (Resource resource : resources) {
				boolean imported = importResource(resource);

				if (imported) {
					importedCount++;
					continue;
				}

				skippedCount++;
			}

			log.info(
				"RAG 리소스 문서 자동 적재 완료 importedCount={}, skippedCount={}",
				importedCount,
				skippedCount
			);
		} catch (IOException e) {
			throw new IllegalStateException("RAG 리소스 문서 자동 적재 중 오류가 발생했습니다.", e);
		}
	}

	private boolean importResource(Resource resource) throws IOException {
		String sourceName = Objects.requireNonNull(resource.getFilename());
		String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

		if (content.isBlank()) {
			log.warn("RAG 리소스 문서 내용이 비어 있어 건너뜁니다. sourceName={}", sourceName);
			return false;
		}

		List<RagDocument> activeDocuments = ragDocumentRepository.findBySourceNameAndActiveTrue(sourceName);

		if (activeDocuments.size() == 1 && activeDocuments.get(0).getContent().equals(content)) {
			log.info("이미 최신 RAG 문서가 적재되어 있어 건너뜁니다. sourceName={}", sourceName);
			return false;
		}

		deactivateExistingDocuments(activeDocuments);

		RagDocument ragDocument = RagDocument.create(
			resolveTitle(sourceName),
			resolveSourceType(sourceName),
			sourceName,
			content
		);

		RagDocument savedDocument = ragDocumentRepository.save(ragDocument);

		List<String> chunkContents = ragChunkSplitter.split(content);

		for (int index = 0; index < chunkContents.size(); index++) {
			RagChunk chunk = RagChunk.create(
				savedDocument,
				index,
				chunkContents.get(index)
			);

			ragChunkRepository.save(chunk);
		}

		log.info(
			"RAG 리소스 문서 적재 완료 sourceName={}, chunkCount={}",
			sourceName,
			chunkContents.size()
		);

		return true;
	}

	private void deactivateExistingDocuments(List<RagDocument> activeDocuments) {
		for (RagDocument document : activeDocuments) {
			ragChunkRepository.findByRagDocumentAndActiveTrueOrderByChunkIndexAsc(document)
				.forEach(RagChunk::deactivate);

			document.deactivate();
		}
	}

	private String resolveTitle(String sourceName) {
		if (sourceName.endsWith(".md")) {
			return sourceName.substring(0, sourceName.length() - 3);
		}

		return sourceName;
	}

	private RagSourceType resolveSourceType(String sourceName) {
		String lowerSourceName = sourceName.toLowerCase();

		if (lowerSourceName.contains("faq")) {
			return RagSourceType.FAQ;
		}

		if (lowerSourceName.contains("policy") || lowerSourceName.contains("guardrails")) {
			return RagSourceType.POLICY;
		}

		if (lowerSourceName.contains("guide")
			|| lowerSourceName.contains("style")
			|| lowerSourceName.contains("synonyms")) {
			return RagSourceType.MANUAL;
		}

		return RagSourceType.ETC;
	}
}