package com.princesses7.findy.recommendation.chatbot.log.repository.projection;

import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

public interface ChatbotIntentCountProjection {

	ChatIntent getIntent();

	Long getCount();
}