package com.princesses7.findy.recommendation.chatbot.log.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLog;
import com.princesses7.findy.recommendation.chatbot.log.entity.ChatbotLogStatus;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotFailureLogProjection;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotFrequentQuestionProjection;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotIntentCountProjection;
import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotLogSummaryProjection;

public interface ChatbotLogRepository extends JpaRepository<ChatbotLog, Long> {

	@Query("""
		SELECT l
		FROM ChatbotLog l
		WHERE (:status IS NULL OR l.status = :status)
			AND (:intent IS NULL OR l.intent = :intent)
			AND l.createdAt >= :fromDateTime
			AND l.createdAt < :toDateTime
		ORDER BY l.createdAt DESC
		""")
	Page<ChatbotLog> searchLogs(
		@Param("status") ChatbotLogStatus status,
		@Param("intent") ChatIntent intent,
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		Pageable pageable
	);

	@Query("""
		SELECT
			COUNT(l) AS totalCount,
			COALESCE(SUM(CASE WHEN l.status = :successStatus THEN 1 ELSE 0 END), 0) AS successCount,
			COALESCE(SUM(CASE WHEN l.status = :failureStatus THEN 1 ELSE 0 END), 0) AS failureCount,
			COALESCE(AVG(l.durationMs), 0) AS averageDurationMs
		FROM ChatbotLog l
		WHERE l.createdAt >= :fromDateTime
			AND l.createdAt < :toDateTime
		""")
	ChatbotLogSummaryProjection getSummary(
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		@Param("successStatus") ChatbotLogStatus successStatus,
		@Param("failureStatus") ChatbotLogStatus failureStatus
	);

	@Query("""
		SELECT
			l.intent AS intent,
			COUNT(l) AS count
		FROM ChatbotLog l
		WHERE l.createdAt >= :fromDateTime
			AND l.createdAt < :toDateTime
			AND l.intent IS NOT NULL
		GROUP BY l.intent
		ORDER BY COUNT(l) DESC
		""")
	List<ChatbotIntentCountProjection> getIntentCounts(
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime
	);

	@Query("""
		SELECT
			l.requestMessage AS question,
			COUNT(l) AS count,
			MAX(l.createdAt) AS lastAskedAt
		FROM ChatbotLog l
		WHERE l.status = :successStatus
			AND l.createdAt >= :fromDateTime
			AND l.createdAt < :toDateTime
		GROUP BY l.requestMessage
		ORDER BY COUNT(l) DESC, MAX(l.createdAt) DESC
		""")
	List<ChatbotFrequentQuestionProjection> getFrequentQuestions(
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		@Param("successStatus") ChatbotLogStatus successStatus,
		Pageable pageable
	);

	@Query("""
		SELECT
			l.chatbotLogId AS chatbotLogId,
			l.userId AS userId,
			l.chatSessionId AS chatSessionId,
			l.intent AS intent,
			l.keyword AS keyword,
			l.requestMessage AS requestMessage,
			l.failureReason AS failureReason,
			l.createdAt AS createdAt
		FROM ChatbotLog l
		WHERE l.status = :failureStatus
			AND l.createdAt >= :fromDateTime
			AND l.createdAt < :toDateTime
		ORDER BY l.createdAt DESC
		""")
	List<ChatbotFailureLogProjection> getFailureLogs(
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		@Param("failureStatus") ChatbotLogStatus failureStatus,
		Pageable pageable
	);
}