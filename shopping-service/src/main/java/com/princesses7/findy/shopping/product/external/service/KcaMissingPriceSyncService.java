package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.kca.KcaProductPriceClient;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceSyncItemResponse;
import com.princesses7.findy.shopping.product.external.dto.response.KcaMissingPriceSyncResponse;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMapping;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchStatus;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalPrice;
import com.princesses7.findy.shopping.product.external.repository.ProductExternalMappingRepository;
import com.princesses7.findy.shopping.product.external.repository.ProductExternalPriceRepository;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class KcaMissingPriceSyncService {

	private static final String EXTERNAL_SOURCE = "KCA";
	private static final BigDecimal MATCHED_THRESHOLD = new BigDecimal("0.8500");
	private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("0.5500");
	private static final DateTimeFormatter KCA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final KcaProductPriceClient kcaProductPriceClient;
	private final ProductRepository productRepository;
	private final ProductExternalMappingRepository mappingRepository;
	private final ProductExternalPriceRepository priceRepository;
	private final RuleBasedProductMatchScorer ruleBasedProductMatchScorer;
	private final OpenAiProductMatchScorer openAiProductMatchScorer;

	public KcaMissingPriceSyncResponse syncMissingPrices(
		String goodInspectDay,
		String entpId,
		String goodId,
		int page,
		Integer size,
		Integer limit
	) {
		KcaProductPriceResponse priceResponse = kcaProductPriceClient.getProductPrices(
			goodInspectDay,
			entpId,
			goodId
		);

		int resolvedPage = resolvePage(page);
		int resolvedSize = resolveSize(size, limit);

		if (priceResponse.items() == null || priceResponse.items().isEmpty()) {
			return KcaMissingPriceSyncResponse.from(List.of(), resolvedPage, resolvedSize, 0);
		}

		List<Product> priceMissingProducts = productRepository.findPriceMissingProducts(PageRequest.of(0, 1000));
		int externalTotalCount = priceResponse.items().size();
		long offset = (long)resolvedPage * resolvedSize;

		List<KcaMissingPriceSyncItemResponse> results = priceResponse.items()
			.stream()
			.skip(offset)
			.limit(resolvedSize)
			.map(priceItem -> syncPriceItem(priceItem, priceMissingProducts))
			.toList();

		return KcaMissingPriceSyncResponse.from(results, resolvedPage, resolvedSize, externalTotalCount);
	}

	private KcaMissingPriceSyncItemResponse syncPriceItem(
		KcaProductPriceItemResponse priceItem,
		List<Product> priceMissingProducts
	) {
		if (priceItem.goodName() == null || priceItem.goodName().isBlank()) {
			return KcaMissingPriceSyncItemResponse.skipped(priceItem, "KCA 상품명 없음");
		}

		Integer price = parsePrice(priceItem.goodPrice());

		if (price == null || price <= 0) {
			return KcaMissingPriceSyncItemResponse.skipped(priceItem, "KCA 가격 없음");
		}

		Optional<ProductMatchCandidate> bestCandidate = findBestCandidate(priceMissingProducts, priceItem);

		if (bestCandidate.isEmpty()) {
			return KcaMissingPriceSyncItemResponse.skipped(priceItem, "가격 미입력 상품 중 매칭 후보 없음");
		}

		ProductMatchCandidate candidate = bestCandidate.get();
		ProductMatchResult finalMatchResult = openAiProductMatchScorer.score(
			candidate.product(),
			priceItem,
			candidate.ruleResult()
		);

		ProductExternalMatchStatus status = resolveStatus(finalMatchResult.confidence());

		if (status == ProductExternalMatchStatus.REJECTED) {
			return KcaMissingPriceSyncItemResponse.skipped(priceItem, "매칭 신뢰도 부족");
		}

		saveMapping(candidate.product(), priceItem, finalMatchResult, status);

		if (status == ProductExternalMatchStatus.REVIEW_REQUIRED) {
			return KcaMissingPriceSyncItemResponse.reviewRequired(
				candidate.product(),
				priceItem,
				status,
				finalMatchResult.confidence()
			);
		}

		Integer beforePrice = candidate.product().getOriginalPrice();
		boolean applied = candidate.product().applyExternalPriceIfMissing(price);

		if (!applied) {
			return KcaMissingPriceSyncItemResponse.skipped(priceItem, "이미 가격이 존재하는 상품");
		}

		savePriceHistory(candidate.product(), priceItem, price);

		return KcaMissingPriceSyncItemResponse.applied(
			candidate.product(),
			beforePrice,
			price,
			priceItem,
			status,
			finalMatchResult.confidence()
		);
	}

	private Optional<ProductMatchCandidate> findBestCandidate(
		List<Product> products,
		KcaProductPriceItemResponse priceItem
	) {
		return products.stream()
			.filter(Product::isPriceMissing)
			.map(product -> new ProductMatchCandidate(
				product,
				ruleBasedProductMatchScorer.score(product, priceItem)
			))
			.max(Comparator.comparing(candidate -> candidate.ruleResult().confidence()));
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

	private void saveMapping(
		Product product,
		KcaProductPriceItemResponse priceItem,
		ProductMatchResult matchResult,
		ProductExternalMatchStatus status
	) {
		ProductExternalMapping mapping = mappingRepository
			.findByExternalSourceAndExternalProductIdAndProductId(
				EXTERNAL_SOURCE,
				priceItem.goodId(),
				product.getProductId()
			)
			.orElseGet(() -> ProductExternalMapping.create(
				product.getProductId(),
				EXTERNAL_SOURCE,
				priceItem.goodId(),
				priceItem.goodName(),
				priceItem.productEntpName(),
				matchResult.confidence(),
				status,
				matchResult.matchedBy(),
				matchResult.reason()
			));

		mapping.updateMatch(
			priceItem.goodName(),
			priceItem.productEntpName(),
			matchResult.confidence(),
			status,
			matchResult.matchedBy(),
			matchResult.reason()
		);

		mappingRepository.save(mapping);
	}

	private void savePriceHistory(
		Product product,
		KcaProductPriceItemResponse priceItem,
		Integer price
	) {
		LocalDate inspectedDate = parseInspectDate(priceItem.goodInspectDay());

		if (inspectedDate == null) {
			return;
		}

		boolean exists = priceRepository.existsByExternalSourceAndExternalProductIdAndExternalStoreIdAndInspectedDate(
			EXTERNAL_SOURCE,
			priceItem.goodId(),
			priceItem.entpId(),
			inspectedDate
		);

		if (exists) {
			return;
		}

		priceRepository.save(ProductExternalPrice.create(
			product.getProductId(),
			EXTERNAL_SOURCE,
			priceItem.goodId(),
			priceItem.entpId(),
			price,
			inspectedDate
		));
	}

	private int resolvePage(int page) {
		if (page <= 0) {
			return 0;
		}

		return page;
	}

	private int resolveSize(Integer size, Integer limit) {
		if (size != null && size > 0) {
			return size;
		}

		if (limit == null || limit <= 0) {
			return 50;
		}

		return limit;
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

	private LocalDate parseInspectDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return LocalDate.parse(value, KCA_DATE_FORMATTER);
		} catch (Exception exception) {
			return null;
		}
	}

	private record ProductMatchCandidate(
		Product product,
		ProductMatchResult ruleResult
	) {
	}
}
