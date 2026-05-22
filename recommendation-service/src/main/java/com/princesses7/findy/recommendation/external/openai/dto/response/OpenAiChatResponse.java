package com.princesses7.findy.recommendation.external.openai.dto.response;

import java.util.List;

public record OpenAiChatResponse(
	List<OpenAiChatChoice> choices
) {

	public String firstContent() {
		if (choices == null || choices.isEmpty()) {
			return "";
		}

		OpenAiChatChoice choice = choices.get(0);

		if (choice == null || choice.message() == null) {
			return "";
		}

		return choice.message().content();
	}
}