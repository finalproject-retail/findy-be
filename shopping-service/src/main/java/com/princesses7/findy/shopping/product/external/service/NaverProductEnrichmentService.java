package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.naver.NaverShoppingClient;
import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentItemResponse;
import com.princesses7.findy.shopping.product.external.dto.response.NaverProductEnrichmentResponse;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchStatus;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverProductEnrichmentService {

	private static final BigDecimal MATCHED_THRESHOLD = new BigDecimal("0.7500");
	private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("0.5000");
	private static final int NAVER_DISPLAY = 5;

	private final ProductRepository productRepository;
	private final NaverShoppingClient naverShoppingClient;
	private final NaverProductMatchScorer matchScorer;

	@Transactional
	public NaverProductEnrichmentResponse enrichMissingProducts(
		int offset,
		int limit
	) {
		int resolvedOffset = Math.max(offset, 0);
		int resolvedLimit = limit <= 0 ? 20 : limit;
		int querySize = resolvedOffset + resolvedLimit + 1;

		List<Product> targets = productRepository.findNaverEnrichmentTargets(PageRequest.of(0, querySize));
		List<Product> batch = targets.stream()
			.skip(resolvedOffset)
			.limit(resolvedLimit)
			.toList();
		boolean hasNext = targets.size() > resolvedOffset + batch.size();

		List<NaverProductEnrichmentItemResponse> items = batch.stream()
			.map(this::enrichProduct)
			.toList();

		return NaverProductEnrichmentResponse.from(items, resolvedOffset, resolvedLimit, hasNext);
	}

	private NaverProductEnrichmentItemResponse enrichProduct(Product product) {
		try {
			NaverShoppingResponse response = naverShoppingClient.search(product.getProductName(), NAVER_DISPLAY, 1);

			if (response == null || response.items() == null || response.items().isEmpty()) {
				return NaverProductEnrichmentItemResponse.skipped(product, "네이버 검색 결과 없음");
			}

			Optional<NaverMatchCandidate> bestCandidate = findBestCandidate(product, response.items());

			if (bestCandidate.isEmpty()) {
				return NaverProductEnrichmentItemResponse.skipped(product, "유효한 네이버 매칭 후보 없음");
			}

			NaverMatchCandidate candidate = bestCandidate.get();
			ProductExternalMatchStatus status = resolveStatus(candidate.matchResult().confidence());
			String cleanTitle = matchScorer.cleanTitle(candidate.item().title());

			if (status == ProductExternalMatchStatus.REJECTED) {
				return NaverProductEnrichmentItemResponse.skipped(product, "매칭 신뢰도 부족");
			}

			if (status == ProductExternalMatchStatus.REVIEW_REQUIRED) {
				return NaverProductEnrichmentItemResponse.reviewRequired(
					product,
					candidate.item(),
					status,
					candidate.matchResult().confidence(),
					cleanTitle
				);
			}

			Integer beforePrice = product.getOriginalPrice();
			String beforeImageUrl = product.getImageUrl();
			List<String> updatedFields = product.enrichFromNaverIfMissing(
				parsePrice(candidate.item().lprice()),
				candidate.item().image(),
				candidate.item().productId()
			);

			if (updatedFields.isEmpty()) {
				return NaverProductEnrichmentItemResponse.skipped(product, "보강할 누락 필드 없음");
			}

			return NaverProductEnrichmentItemResponse.applied(
				product,
				beforePrice,
				beforeImageUrl,
				candidate.item(),
				status,
				candidate.matchResult().confidence(),
				updatedFields,
				cleanTitle
			);
		} catch (Exception exception) {
			log.warn("Naver product enrichment skipped. productId={}", product.getProductId(), exception);
			return NaverProductEnrichmentItemResponse.skipped(
				product,
				exception.getClass().getSimpleName() + ": " + exception.getMessage()
			);
		}
	}

	private Optional<NaverMatchCandidate> findBestCandidate(
		Product product,
		List<NaverShoppingItemResponse> items
	) {
		return items.stream()
			.filter(this::isValid)
			.map(item -> new NaverMatchCandidate(item, matchScorer.score(product, item)))
			.max(Comparator.comparing(candidate -> candidate.matchResult().confidence()));
	}

	private boolean isValid(NaverShoppingItemResponse item) {
		return item.productId() != null && !item.productId().isBlank()
			&& item.title() != null && !item.title().isBlank()
			&& item.lprice() != null && !item.lprice().isBlank();
	}

	private ProductExternalMatchStatus resolveStatus(BigDecimal confidence) {
		if (confidence.compareTo(MATCHED_THRESHOLD) >= 0) {
			return ProductExternalMatchStatus.MATCHED;
		}

		if (confidence.compareTo(REVIEW_THRESHOLD) >= 0) {
			return ProductExternalMatchStatus.REVIEW_REQUIRED;
		}

		return ProductExternalMatchStatus.REJECTED;
	}

	private Integer parsePrice(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private record NaverMatchCandidate(
		NaverShoppingItemResponse item,
		ProductMatchResult matchResult
	) {
	}
}
