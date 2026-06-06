package com.princesses7.findy.recommendation.chatbot.rag.dto.response;

import java.util.List;

public record RagContextResponse(
	String query,
	List<RagSearchResultResponse> results
) {

	public boolean hasData() {
		return results != null && !results.isEmpty();
	}

	public static RagContextResponse empty(String query) {
		return new RagContextResponse(query, List.of());
	}
}