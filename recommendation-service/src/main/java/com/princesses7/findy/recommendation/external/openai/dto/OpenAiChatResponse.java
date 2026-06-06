package com.princesses7.findy.recommendation.external.openai.dto;

import java.util.List;

public record OpenAiChatResponse(
	List<OpenAiChatChoice> choices
) {

	public String firstAnswer() {
		if (choices == null || choices.isEmpty()) {
			return "죄송해요. 지금은 답변을 생성하지 못했어요.";
		}

		OpenAiChatChoice choice = choices.get(0);
		if (choice == null || choice.message() == null || choice.message().content() == null) {
			return "죄송해요. 지금은 답변을 생성하지 못했어요.";
		}

		return choice.message().content();
	}
}