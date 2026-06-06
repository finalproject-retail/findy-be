package com.princesses7.findy.recommendation.chatbot.rag.repository;

import java.util.List;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagSearchResultResponse;

public interface RagSearchRepository {

	List<RagSearchResultResponse> search(String query, int limit);
}