package com.princesses7.findy.recommendation.recommendation.validator;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

@Component
public class RecommendationRequestValidator {

	public void validatePositiveId(
		Long value,
		String fieldName
	) {
		if (value == null || value <= 0) {
			throw new BaseException(
				ErrorCode.RECOMMENDATION_INVALID_REQUEST,
				fieldName + "은(는) 1 이상의 값이어야 합니다."
			);
		}
	}

	public int normalizeSize(
		int size,
		int defaultSize,
		int maxSize
	) {
		if (size <= 0) {
			return defaultSize;
		}

		return Math.min(size, maxSize);
	}
}