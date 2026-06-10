package com.princesses7.findy.shopping.external.kca;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceResponse;
import com.princesses7.findy.shopping.global.config.KcaProductPriceProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KcaProductPriceClient {

	private static final String PRODUCT_PRICE_PATH =
		"/openApiImpl/ProductPriceInfoService/getProductPriceInfoSvc.do";

	private final RestClient kcaProductPriceRestClient;
	private final KcaProductPriceProperties properties;
	private final KcaProductPriceXmlParser xmlParser;
	private final KcaPriceDateResolver dateResolver;
	private final KcaProductInfoClient productInfoClient;

	public KcaProductPriceClient(
		@Qualifier("kcaProductPriceRestClient") RestClient kcaProductPriceRestClient,
		KcaProductPriceProperties properties,
		KcaProductPriceXmlParser xmlParser,
		KcaPriceDateResolver dateResolver,
		KcaProductInfoClient productInfoClient
	) {
		this.kcaProductPriceRestClient = kcaProductPriceRestClient;
		this.properties = properties;
		this.xmlParser = xmlParser;
		this.dateResolver = dateResolver;
		this.productInfoClient = productInfoClient;
	}

	public KcaProductPriceResponse getProductPrices(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		KcaProductPriceResponse response = getProductPricesWithoutEnrichment(goodInspectDay, entpId, goodId);

		if (!xmlParser.isSuccess(response)) {
			return response;
		}

		return enrichProductInfo(response);
	}

	public KcaProductPriceResponse getProductPricesByProductInfos(
		String goodInspectDay,
		List<KcaProductInfoItemResponse> productInfos
	) {
		if (productInfos == null || productInfos.isEmpty()) {
			return new KcaProductPriceResponse(null, "empty product infos", List.of());
		}

		Map<String, KcaProductInfoItemResponse> productInfoMap = productInfos.stream()
			.collect(Collectors.toMap(
				KcaProductInfoItemResponse::goodId,
				productInfo -> productInfo,
				(first, second) -> first
			));

		List<KcaProductPriceItemResponse> items = productInfos.stream()
			.map(productInfo -> getProductPricesWithoutEnrichment(goodInspectDay, null, productInfo.goodId()))
			.filter(xmlParser::isSuccess)
			.flatMap(response -> response.items().stream())
			.map(item -> enrichItem(item, productInfoMap.get(item.goodId())))
			.toList();

		return new KcaProductPriceResponse("00", "ok", items);
	}

	public boolean hasAnyProductPrice(
		String goodInspectDay,
		List<KcaProductInfoItemResponse> productInfos,
		int sampleSize
	) {
		if (productInfos == null || productInfos.isEmpty()) {
			return false;
		}

		return productInfos.stream()
			.limit(Math.max(sampleSize, 1))
			.map(productInfo -> getProductPricesWithoutEnrichment(goodInspectDay, null, productInfo.goodId()))
			.anyMatch(response -> xmlParser.isSuccess(response)
				&& response.items() != null
				&& !response.items().isEmpty());
	}

	private KcaProductPriceResponse getProductPricesWithoutEnrichment(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		try {
			String xml = kcaProductPriceRestClient.get()
				.uri(buildProductPriceUri(goodInspectDay, entpId, goodId))
				.retrieve()
				.body(String.class);

			KcaProductPriceResponse response = xmlParser.parse(xml);

			if (!xmlParser.isSuccess(response)) {
				log.warn(
					"KCA product price API returned non-success. resultCode={}, resultMessage={}",
					response.resultCode(),
					response.resultMessage()
				);

				return response;
			}

			return response;
		} catch (Exception exception) {
			log.warn("KCA product price API request failed. message={}", exception.getMessage());

			return new KcaProductPriceResponse(null, exception.getMessage(), List.of());
		}
	}

	public KcaProductPriceResponse getLatestProductPrices(
		String entpId,
		String goodId
	) {
		return getProductPrices(dateResolver.resolveLatestFriday(), entpId, goodId);
	}

	public List<KcaProductPriceItemResponse> getLatestProductPriceItems(
		String entpId,
		String goodId
	) {
		return getLatestProductPrices(entpId, goodId).items();
	}

	public String getRawProductPrices(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		try {
			return kcaProductPriceRestClient.get()
				.uri(buildProductPriceUri(goodInspectDay, entpId, goodId))
				.retrieve()
				.body(String.class);
		} catch (Exception exception) {
			log.warn("KCA raw product price API request failed. message={}", exception.getMessage());

			return "KCA raw product price API request failed: " + exception.getMessage();
		}
	}

	private KcaProductPriceResponse enrichProductInfo(KcaProductPriceResponse response) {
		if (response == null || response.items() == null || response.items().isEmpty()) {
			return response;
		}

		Map<String, KcaProductInfoItemResponse> productInfoMap = productInfoClient.getProductInfos()
			.stream()
			.collect(Collectors.toMap(
				KcaProductInfoItemResponse::goodId,
				productInfo -> productInfo,
				(first, second) -> first
			));

		List<KcaProductPriceItemResponse> enrichedItems = response.items()
			.stream()
			.map(item -> enrichItem(item, productInfoMap.get(item.goodId())))
			.toList();

		return new KcaProductPriceResponse(
			response.resultCode(),
			response.resultMessage(),
			enrichedItems
		);
	}

	private KcaProductPriceItemResponse enrichItem(
		KcaProductPriceItemResponse item,
		KcaProductInfoItemResponse productInfo
	) {
		if (productInfo == null) {
			return item;
		}

		return new KcaProductPriceItemResponse(
			item.goodInspectDay(),
			item.goodId(),
			productInfo.goodName(),
			item.entpId(),
			item.entpName(),
			productInfo.productEntpCode(),
			productInfo.productEntpName(),
			item.goodPrice(),
			item.plusoneYn(),
			item.saleYn(),
			item.goodDcYn(),
			item.goodDcStartDay(),
			item.goodDcEndDay(),
			item.inputDttm()
		);
	}

	private URI buildProductPriceUri(
		String goodInspectDay,
		String entpId,
		String goodId
	) {
		if (!StringUtils.hasText(properties.serviceKey())) {
			throw new IllegalStateException("KCA_PRODUCT_PRICE_SERVICE_KEY 환경변수가 설정되지 않았습니다.");
		}

		StringBuilder uri = new StringBuilder(removeTrailingSlash(properties.baseUrl()))
			.append(PRODUCT_PRICE_PATH)
			.append("?goodInspectDay=")
			.append(goodInspectDay)
			.append("&ServiceKey=")
			.append(properties.serviceKey());

		if (StringUtils.hasText(entpId)) {
			uri.append("&entpId=").append(entpId);
		}

		if (StringUtils.hasText(goodId)) {
			uri.append("&goodId=").append(goodId);
		}

		return URI.create(uri.toString());
	}

	private String removeTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}

		return value;
	}
}
