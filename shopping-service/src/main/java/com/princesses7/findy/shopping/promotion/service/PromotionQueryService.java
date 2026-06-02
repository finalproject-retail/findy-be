package com.princesses7.findy.shopping.promotion.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.promotion.dto.response.ApplicablePromotionResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionProductPageResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionProductResponse;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionQueryService {

	private final PromotionProductRepository promotionProductRepository;

	public PromotionProductPageResponse getActivePromotionProducts(
		int page,
		int size
	) {
		validatePageRequest(page, size);

		Pageable pageable = PageRequest.of(page, size);
		LocalDateTime now = LocalDateTime.now();

		Page<PromotionProductResponse> promotionProducts = promotionProductRepository
			.findActivePromotionProducts(
				PromotionStatus.ENDED,
				now,
				pageable
			)
			.map(PromotionProductResponse::from);

		return PromotionProductPageResponse.from(promotionProducts);
	}

	public List<ApplicablePromotionResponse> getApplicablePromotions(Long productId) {
		if (productId == null) {
			throw new BaseException(INVALID_PROMOTION_REQUEST);
		}

		LocalDateTime now = LocalDateTime.now();

		List<PromotionProduct> promotionProducts = promotionProductRepository
			.findApplicablePromotionProducts(
				productId,
				PromotionStatus.ENDED,
				now
			);

		return promotionProducts.stream()
			.map(ApplicablePromotionResponse::from)
			.toList();
	}

	private void validatePageRequest(
		int page,
		int size
	) {
		if (page < 0 || size < 1 || size > 100) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}
	}
}
