package com.princesses7.findy.shopping.external.kca;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoItemResponse;
import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductInfoResponse;
import com.princesses7.findy.shopping.global.config.KcaProductPriceProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KcaProductInfoClient {

	private static final String PRODUCT_INFO_PATH =
		"/openApiImpl/ProductPriceInfoService/getProductInfoSvc.do";

	@Qualifier("kcaProductPriceRestClient")
	private final RestClient kcaProductPriceRestClient;

	private final KcaProductPriceProperties properties;
	private final KcaProductInfoXmlParser xmlParser;

	public List<KcaProductInfoItemResponse> getProductInfos() {
		try {
			String xml = kcaProductPriceRestClient.get()
				.uri(buildProductInfoUri())
				.retrieve()
				.body(String.class);

			KcaProductInfoResponse response = xmlParser.parse(xml);

			return response.items();
		} catch (Exception exception) {
			log.warn("KCA product info API request failed. message={}", exception.getMessage());

			return List.of();
		}
	}

	private URI buildProductInfoUri() {
		if (!StringUtils.hasText(properties.serviceKey())) {
			throw new IllegalStateException("KCA_PRODUCT_PRICE_SERVICE_KEY 환경변수가 설정되지 않았습니다.");
		}

		String uri = removeTrailingSlash(properties.baseUrl())
			+ PRODUCT_INFO_PATH
			+ "?ServiceKey="
			+ properties.serviceKey();

		return URI.create(uri);
	}

	private String removeTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}

		return value;
	}
}