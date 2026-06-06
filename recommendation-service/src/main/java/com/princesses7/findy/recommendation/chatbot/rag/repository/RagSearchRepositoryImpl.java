package com.princesses7.findy.recommendation.chatbot.rag.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.recommendation.chatbot.rag.dto.response.RagSearchResultResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RagSearchRepositoryImpl implements RagSearchRepository {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public List<RagSearchResultResponse> search(String query, int limit) {
		if (query == null || query.isBlank()) {
			return List.of();
		}

		String normalizedQuery = normalizeQuery(query);

		if (normalizedQuery.isBlank()) {
			return List.of();
		}

		List<RagSearchResultResponse> fullTextResults = searchByFullText(
			normalizedQuery,
			limit
		);

		if (!fullTextResults.isEmpty()) {
			return fullTextResults;
		}

		return searchByLike(
			normalizedQuery,
			limit
		);
	}

	private List<RagSearchResultResponse> searchByFullText(
		String query,
		int limit
	) {
		String sql = """
			SELECT
				d.rag_document_id,
				c.rag_chunk_id,
				d.title,
				d.source_type,
				d.source_name,
				c.chunk_index,
				c.content,
				CAST((ts_rank(
					to_tsvector('simple', c.content),
					plainto_tsquery('simple', ?)
				) * 1000) AS INTEGER) AS score
			FROM chatbot_rag_chunks c
			JOIN chatbot_rag_documents d
				ON c.rag_document_id = d.rag_document_id
			WHERE c.is_active = TRUE
				AND d.is_active = TRUE
				AND to_tsvector('simple', c.content) @@ plainto_tsquery('simple', ?)
			ORDER BY score DESC, c.updated_at DESC
			LIMIT ?
			""";

		return jdbcTemplate.query(
			sql,
			(rs, rowNum) -> new RagSearchResultResponse(
				rs.getLong("rag_document_id"),
				rs.getLong("rag_chunk_id"),
				rs.getString("title"),
				rs.getString("source_type"),
				rs.getString("source_name"),
				rs.getInt("chunk_index"),
				rs.getString("content"),
				rs.getInt("score")
			),
			query,
			query,
			limit
		);
	}

	private List<RagSearchResultResponse> searchByLike(
		String query,
		int limit
	) {
		String[] words = query.split("\\s+");

		List<String> conditions = new ArrayList<>();
		List<Object> params = new ArrayList<>();

		for (String word : words) {
			if (word == null || word.isBlank()) {
				continue;
			}

			conditions.add("LOWER(c.content) LIKE LOWER(?)");
			params.add("%" + word + "%");
		}

		if (conditions.isEmpty()) {
			return List.of();
		}

		String sql = """
			SELECT
				d.rag_document_id,
				c.rag_chunk_id,
				d.title,
				d.source_type,
				d.source_name,
				c.chunk_index,
				c.content,
				100 AS score
			FROM chatbot_rag_chunks c
			JOIN chatbot_rag_documents d
				ON c.rag_document_id = d.rag_document_id
			WHERE c.is_active = TRUE
				AND d.is_active = TRUE
				AND (
			"""
			+ String.join(" OR ", conditions)
			+ """
				)
			ORDER BY c.updated_at DESC
			LIMIT ?
			""";

		params.add(limit);

		return jdbcTemplate.query(
			sql,
			(rs, rowNum) -> new RagSearchResultResponse(
				rs.getLong("rag_document_id"),
				rs.getLong("rag_chunk_id"),
				rs.getString("title"),
				rs.getString("source_type"),
				rs.getString("source_name"),
				rs.getInt("chunk_index"),
				rs.getString("content"),
				rs.getInt("score")
			),
			params.toArray()
		);
	}

	private String normalizeQuery(String query) {
		return query
			.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}
}