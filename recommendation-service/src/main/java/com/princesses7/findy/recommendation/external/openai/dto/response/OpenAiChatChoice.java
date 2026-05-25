package com.princesses7.findy.recommendation.external.openai.dto.response;

import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

public record OpenAiChatChoice(
	OpenAiChatMessage message
) {
}