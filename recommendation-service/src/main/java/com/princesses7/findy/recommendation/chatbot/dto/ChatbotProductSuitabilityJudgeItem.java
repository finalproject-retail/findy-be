package com.princesses7.findy.recommendation.chatbot.dto;

public record ChatbotProductSuitabilityJudgeItem(
	Long productId,
	Boolean suitable,
	Double confidence,
	String reason
) {

	public boolean isSuitable() {
		return Boolean.TRUE.equals(suitable);
	}

	public double safeConfidence() {
		if (confidence == null) {
			return 0.0;
		}

		return Math.max(0.0, Math.min(confidence, 1.0));
	}

	public String safeReason() {
		if (reason == null || reason.isBlank()) {
			return "";
		}

		return reason.trim();
	}
}