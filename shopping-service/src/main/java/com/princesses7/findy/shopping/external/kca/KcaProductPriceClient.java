package com.princesses7.findy.shopping.external.kca;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceResponse;
import com.princesses7.findy.shopping.global.config.KcaProductPriceProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KcaProductPriceClient {

	private static final String PRODUCT_PRICE_PATH =
		"/openApiImpl/ProductPriceInfoService/getProductPriceInfoSvc.do";

	@Qualifier("kcaProductPriceRestClient")
	private final RestClient kcaProductPriceRestClient;

	private final KcaProductPriceProperties properties;
	private final KcaProductPriceXmlParser xmlParser;
	private final KcaPriceDateResolver dateResolver;

	public KcaProductPriceResponse getProductPrices(
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