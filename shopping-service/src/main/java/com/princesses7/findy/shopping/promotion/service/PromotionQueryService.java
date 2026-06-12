package com.princesses7.findy.shopping.promotion.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.promotion.dto.response.ApplicablePromotionResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionMapMarkerListResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionMapMarkerResponse;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionQueryService {

	private final PromotionProductRepository promotionProductRepository;

	public PromotionMapMarkerListResponse getActivePromotionMapMarkers() {
		LocalDateTime now = LocalDateTime.now();

		List<PromotionMapMarkerResponse> markers = promotionProductRepository
			.findActivePromotionMapMarkers(
				PromotionStatus.ENDED,
				now
			)
			.stream()
			.map(PromotionMapMarkerResponse::from)
			.toList();

		return PromotionMapMarkerListResponse.from(markers);
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
}