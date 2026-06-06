package com.princesses7.findy.recommendation.chatbot.log.repository.projection;

import java.time.LocalDateTime;

public interface ChatbotFrequentQuestionProjection {

	String getQuestion();

	Long getCount();

	LocalDateTime getLastAskedAt();
}