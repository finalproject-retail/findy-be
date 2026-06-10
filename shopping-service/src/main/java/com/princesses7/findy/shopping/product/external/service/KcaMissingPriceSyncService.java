package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.kca.KcaProductInfoClient;
import com.princesses7.findy.shopping.external.kca.KcaProductPriceClient;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoItemResponse;
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
	private static final String AGGREGATED_STORE_ID = "AGGREGATED";
	private static final BigDecimal MATCHED_THRESHOLD = new BigDecimal("0.7000");
	private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("0.3000");
	private static final DateTimeFormatter KCA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final int LATEST_INSPECT_DAY_SEARCH_WEEKS = 26;
	private static final int LATEST_INSPECT_DAY_SAMPLE_SIZE = 20;

	private final KcaProductPriceClient kcaProductPriceClient;
	private final KcaProductInfoClient kcaProductInfoClient;
	private final ProductRepository productRepository;
	private final ProductExternalMappingRepository mappingRepository;
	private final ProductExternalPriceRepository priceRepository;
	private final RuleBasedProductMatchScorer ruleBasedProductMatchScorer;
	private final OpenAiProductMatchScorer openAiProductMatchScorer;

	public KcaMissingPriceSyncResponse syncMissingPrices(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		KcaProductPriceResponse priceResponse = findProductPrices(
			goodInspectDay,
			entpId,
			goodId
		);

		if (priceResponse.items() == null || priceResponse.items().isEmpty()) {
			return KcaMissingPriceSyncResponse.from(
				List.of(),
				resolveInspectDay(priceResponse),
				0,
				0,
				priceResponse.resultCode(),
				priceResponse.resultMessage()
			);
		}

		List<Product> priceMissingProducts = productRepository.findPriceMissingProducts(PageRequest.of(0, 1000));
		List<KcaProductPriceItemResponse> aggregatedItems = aggregateByProduct(priceResponse.items());

		List<KcaMissingPriceSyncItemResponse> results = aggregatedItems
			.stream()
			.map(priceItem -> syncPriceItem(priceItem, priceMissingProducts))
			.toList();

		return KcaMissingPriceSyncResponse.from(
			results,
			resolveInspectDay(priceResponse),
			priceResponse.items().size(),
			aggregatedItems.size(),
			priceResponse.resultCode(),
			priceResponse.resultMessage()
		);
	}

	private KcaProductPriceResponse findProductPrices(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		if (hasText(entpId) || hasText(goodId)) {
			return findProductPricesByFilter(goodInspectDay, entpId, goodId);
		}

		List<KcaProductInfoItemResponse> productInfos = kcaProductInfoClient.getProductInfos();

		if (hasText(goodInspectDay)) {
			return kcaProductPriceClient.getProductPricesByProductInfos(goodInspectDay, productInfos);
		}

		return findLatestProductPrices(productInfos);
	}

	private KcaProductPriceResponse findProductPricesByFilter(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		if (hasText(goodInspectDay)) {
			return kcaProductPriceClient.getProductPrices(goodInspectDay, entpId, goodId);
		}

		LocalDate inspectDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));

		for (int index = 0; index < LATEST_INSPECT_DAY_SEARCH_WEEKS; index++) {
			String candidateInspectDay = inspectDay.minusWeeks(index).format(KCA_DATE_FORMATTER);
			KcaProductPriceResponse response = kcaProductPriceClient.getProductPrices(
				candidateInspectDay,
				entpId,
				goodId
			);

			if (response.items() != null && !response.items().isEmpty()) {
				return response;
			}
		}

		return new KcaProductPriceResponse(null, "latest inspect day not found", List.of());
	}

	private KcaProductPriceResponse findLatestProductPrices(List<KcaProductInfoItemResponse> productInfos) {
		if (productInfos == null || productInfos.isEmpty()) {
			return new KcaProductPriceResponse(null, "empty product infos", List.of());
		}

		LocalDate inspectDay = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));

		for (int index = 0; index < LATEST_INSPECT_DAY_SEARCH_WEEKS; index++) {
			String candidateInspectDay = inspectDay.minusWeeks(index).format(KCA_DATE_FORMATTER);

			if (kcaProductPriceClient.hasAnyProductPrice(
				candidateInspectDay,
				productInfos,
				LATEST_INSPECT_DAY_SAMPLE_SIZE
			)) {
				return kcaProductPriceClient.getProductPricesByProductInfos(candidateInspectDay, productInfos);
			}
		}

		return new KcaProductPriceResponse(null, "latest inspect day not found", List.of());
	}

	private List<KcaProductPriceItemResponse> aggregateByProduct(List<KcaProductPriceItemResponse> items) {
		Map<String, List<KcaProductPriceItemResponse>> itemsByProduct = new LinkedHashMap<>();

		for (KcaProductPriceItemResponse item : items) {
			String key = resolveProductKey(item);

			if (!hasText(key)) {
				continue;
			}

			itemsByProduct.computeIfAbsent(key, ignored -> new ArrayList<>())
				.add(item);
		}

		return itemsByProduct.values()
			.stream()
			.map(this::aggregateProductItems)
			.flatMap(Optional::stream)
			.toList();
	}

	private Optional<KcaProductPriceItemResponse> aggregateProductItems(List<KcaProductPriceItemResponse> items) {
		if (items.isEmpty()) {
			return Optional.empty();
		}

		KcaProductPriceItemResponse representative = items.get(0);
		Integer medianPrice = calculateMedianPrice(items);

		if (medianPrice == null) {
			return Optional.empty();
		}

		return Optional.of(new KcaProductPriceItemResponse(
			representative.goodInspectDay(),
			representative.goodId(),
			representative.goodName(),
			AGGREGATED_STORE_ID,
			AGGREGATED_STORE_ID,
			representative.productEntpCode(),
			representative.productEntpName(),
			String.valueOf(medianPrice),
			null,
			null,
			null,
			null,
			null,
			representative.inputDttm()
		));
	}

	private Integer calculateMedianPrice(List<KcaProductPriceItemResponse> items) {
		List<Integer> prices = items.stream()
			.map(KcaProductPriceItemResponse::goodPrice)
			.map(this::parsePrice)
			.filter(price -> price != null && price > 0)
			.sorted()
			.toList();

		if (prices.isEmpty()) {
			return null;
		}

		int middleIndex = prices.size() / 2;

		if (prices.size() % 2 == 1) {
			return prices.get(middleIndex);
		}

		return (prices.get(middleIndex - 1) + prices.get(middleIndex)) / 2;
	}

	private String resolveProductKey(KcaProductPriceItemResponse item) {
		if (hasText(item.goodId())) {
			return item.goodId();
		}

		return item.goodName();
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

	private String resolveInspectDay(KcaProductPriceResponse response) {
		if (response.items() == null || response.items().isEmpty()) {
			return null;
		}

		return response.items().get(0).goodInspectDay();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private record ProductMatchCandidate(
		Product product,
		ProductMatchResult ruleResult
	) {
	}
}
