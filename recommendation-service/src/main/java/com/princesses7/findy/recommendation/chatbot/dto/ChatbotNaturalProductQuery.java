package com.princesses7.findy.recommendation.chatbot.dto;

import java.util.List;
import java.util.stream.Stream;

public record ChatbotNaturalProductQuery(
	String naturalLanguageQuery,
	List<String> categoryKeywords,
	List<String> productKeywords,
	List<String> preferenceKeywords
) {

	public ChatbotNaturalProductQuery {
		naturalLanguageQuery = normalizeText(naturalLanguageQuery);
		categoryKeywords = normalizeKeywords(categoryKeywords);
		productKeywords = normalizeKeywords(productKeywords);
		preferenceKeywords = normalizeKeywords(preferenceKeywords);
	}

	public boolean hasSearchKeywords() {
		return !searchKeywords().isEmpty();
	}

	public List<String> searchKeywords() {
		return Stream.of(
				categoryKeywords,
				productKeywords,
				preferenceKeywords
			)
			.flatMap(List::stream)
			.filter(ChatbotNaturalProductQuery::hasText)
			.distinct()
			.toList();
	}

	public static ChatbotNaturalProductQuery fallback(String message) {
		String normalizedMessage = normalizeText(message);

		if (!hasText(normalizedMessage)) {
			return new ChatbotNaturalProductQuery(
				null,
				List.of(),
				List.of(),
				List.of()
			);
		}

		return new ChatbotNaturalProductQuery(
			normalizedMessage,
			List.of(),
			List.of(normalizedMessage),
			List.of()
		);
	}

	private static List<String> normalizeKeywords(List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return List.of();
		}

		return keywords.stream()
			.map(ChatbotNaturalProductQuery::normalizeText)
			.filter(ChatbotNaturalProductQuery::hasText)
			.distinct()
			.toList();
	}

	private static String normalizeText(String value) {
		if (value == null) {
			return null;
		}

		String normalizedValue = value.trim();

		if (normalizedValue.isBlank()) {
			return null;
		}

		return normalizedValue;
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}