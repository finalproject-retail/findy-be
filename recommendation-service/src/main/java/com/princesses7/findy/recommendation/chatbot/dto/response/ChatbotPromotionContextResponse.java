package com.princesses7.findy.recommendation.chatbot.dto.response;

public record ChatbotPromotionContextResponse(
	Long promotionId,
	String promotionName,
	String promotionType,
	Integer promotionPrice,
	String benefitText
) {
}