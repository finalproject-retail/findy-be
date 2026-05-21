package com.princesses7.findy.shopping.external.mfds;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.global.config.MfdsBarcodeProperties;
import com.princesses7.findy.shopping.product.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfdsBarcodeClient {

	private final RestClient mfdsBarcodeRestClient;
	private final MfdsBarcodeProperties properties;

	public MfdsBarcodeResponse searchByBarcode(String barcode) {
		try {
			return mfdsBarcodeRestClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/{keyId}/{serviceId}/{dataType}/{startIdx}/{endIdx}")
					.queryParam("BAR_CD", barcode)
					.build(
						properties.keyId(),
						properties.serviceId(),
						properties.dataType(),
						1,
						10
					))
				.retrieve()
				.body(MfdsBarcodeResponse.class);
		} catch (RestClientException exception) {
			throw new ProductException(BARCODE_PRODUCT_NOT_FOUND);
		}
	}
}