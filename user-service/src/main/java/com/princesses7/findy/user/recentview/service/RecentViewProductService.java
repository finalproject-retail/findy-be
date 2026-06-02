package com.princesses7.findy.user.recentview.service;

import static com.princesses7.findy.user.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.recentview.client.ShoppingProductClient;
import com.princesses7.findy.user.recentview.dto.request.AddRecentViewProductRequest;
import com.princesses7.findy.user.recentview.dto.response.ProductSummaryResponse;
import com.princesses7.findy.user.recentview.dto.response.RecentViewProductListResponse;
import com.princesses7.findy.user.recentview.dto.response.RecentViewProductResponse;
import com.princesses7.findy.user.recentview.entity.RecentViewProduct;
import com.princesses7.findy.user.recentview.repository.RecentViewProductRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewProductService {

	private static final int MAX_RECENT_VIEW_LIMIT = 50;

	private final UserRepository userRepository;
	private final RecentViewProductRepository recentViewProductRepository;
	private final ShoppingProductClient shoppingProductClient;

	@Transactional
	public void addRecentViewProduct(
		Long userId,
		AddRecentViewProductRequest request
	) {
		validateUser(userId);

		Long productId = request.productId();

		recentViewProductRepository.findByUserIdAndProductId(userId, productId)
			.ifPresentOrElse(
				RecentViewProduct::refreshViewedAt,
				() -> recentViewProductRepository.save(
					RecentViewProduct.create(userId, productId)
				)
			);
	}

	public RecentViewProductListResponse getRecentViewProducts(
		Long userId,
		int limit
	) {
		validateUser(userId);
		validateLimit(limit);

		List<RecentViewProduct> recentViewProducts =
			recentViewProductRepository.findAllByUserIdOrderByViewedAtDescRecentViewIdDesc(
				userId,
				PageRequest.of(0, limit)
			);

		if (recentViewProducts.isEmpty()) {
			return RecentViewProductListResponse.from(List.of());
		}

		List<Long> productIds = recentViewProducts.stream()
			.map(RecentViewProduct::getProductId)
			.toList();

		Map<Long, ProductSummaryResponse> productSummaryMap =
			shoppingProductClient.getProductSummaryMap(productIds);

		List<RecentViewProductResponse> responses = recentViewProducts.stream()
			.map(recentViewProduct -> RecentViewProductResponse.from(
				recentViewProduct,
				productSummaryMap.get(recentViewProduct.getProductId())
			))
			.toList();

		return RecentViewProductListResponse.from(responses);
	}

	private void validateUser(Long userId) {
		if (userId == null || userId < 1) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}

		if (!userRepository.existsById(userId)) {
			throw new BaseException(USER_NOT_FOUND);
		}
	}

	private void validateLimit(int limit) {
		if (limit < 1 || limit > MAX_RECENT_VIEW_LIMIT) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}
	}
}