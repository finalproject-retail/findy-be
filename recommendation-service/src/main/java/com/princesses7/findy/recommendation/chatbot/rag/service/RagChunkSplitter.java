package com.princesses7.findy.recommendation.chatbot.rag.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RagChunkSplitter {

	private static final int MAX_CHUNK_LENGTH = 700;
	private static final int MIN_CHUNK_LENGTH = 120;

	public List<String> split(String content) {
		if (content == null || content.isBlank()) {
			return List.of();
		}

		String normalizedContent = normalize(content);

		if (normalizedContent.length() <= MAX_CHUNK_LENGTH) {
			return List.of(normalizedContent);
		}

		List<String> chunks = new ArrayList<>();
		StringBuilder currentChunk = new StringBuilder();

		String[] paragraphs = normalizedContent.split("\\n{2,}");

		for (String paragraph : paragraphs) {
			String trimmedParagraph = paragraph.trim();

			if (trimmedParagraph.isBlank()) {
				continue;
			}

			if (trimmedParagraph.length() > MAX_CHUNK_LENGTH) {
				flushCurrentChunk(chunks, currentChunk);
				chunks.addAll(splitLongParagraph(trimmedParagraph));
				continue;
			}

			if (currentChunk.length() + trimmedParagraph.length() + 2 > MAX_CHUNK_LENGTH) {
				flushCurrentChunk(chunks, currentChunk);
			}

			if (!currentChunk.isEmpty()) {
				currentChunk.append("\n\n");
			}

			currentChunk.append(trimmedParagraph);
		}

		flushCurrentChunk(chunks, currentChunk);

		return mergeTooSmallChunks(chunks);
	}

	private List<String> splitLongParagraph(String paragraph) {
		List<String> chunks = new ArrayList<>();
		StringBuilder currentChunk = new StringBuilder();

		String[] sentences = paragraph.split("(?<=[.!?。！？])\\s+");

		for (String sentence : sentences) {
			String trimmedSentence = sentence.trim();

			if (trimmedSentence.isBlank()) {
				continue;
			}

			if (trimmedSentence.length() > MAX_CHUNK_LENGTH) {
				flushCurrentChunk(chunks, currentChunk);
				chunks.addAll(splitByLength(trimmedSentence));
				continue;
			}

			if (currentChunk.length() + trimmedSentence.length() + 1 > MAX_CHUNK_LENGTH) {
				flushCurrentChunk(chunks, currentChunk);
			}

			if (!currentChunk.isEmpty()) {
				currentChunk.append(' ');
			}

			currentChunk.append(trimmedSentence);
		}

		flushCurrentChunk(chunks, currentChunk);

		return chunks;
	}

	private List<String> splitByLength(String text) {
		List<String> chunks = new ArrayList<>();

		int start = 0;

		while (start < text.length()) {
			int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
			chunks.add(text.substring(start, end).trim());
			start = end;
		}

		return chunks;
	}

	private List<String> mergeTooSmallChunks(List<String> chunks) {
		if (chunks.size() <= 1) {
			return chunks;
		}

		List<String> mergedChunks = new ArrayList<>();

		for (String chunk : chunks) {
			if (mergedChunks.isEmpty()) {
				mergedChunks.add(chunk);
				continue;
			}

			String previousChunk = mergedChunks.get(mergedChunks.size() - 1);

			if (chunk.length() < MIN_CHUNK_LENGTH
				&& previousChunk.length() + chunk.length() + 2 <= MAX_CHUNK_LENGTH) {
				mergedChunks.set(
					mergedChunks.size() - 1,
					previousChunk + "\n\n" + chunk
				);
				continue;
			}

			mergedChunks.add(chunk);
		}

		return mergedChunks;
	}

	private void flushCurrentChunk(
		List<String> chunks,
		StringBuilder currentChunk
	) {
		if (currentChunk.isEmpty()) {
			return;
		}

		chunks.add(currentChunk.toString().trim());
		currentChunk.setLength(0);
	}

	private String normalize(String content) {
		return content
			.replace("\r\n", "\n")
			.replace("\r", "\n")
			.replaceAll("[ \\t]+", " ")
			.trim();
	}
}