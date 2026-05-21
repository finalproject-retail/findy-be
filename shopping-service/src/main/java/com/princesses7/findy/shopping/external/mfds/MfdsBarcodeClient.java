package com.princesses7.findy.shopping.external.mfds;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownContentTypeException;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.global.config.MfdsBarcodeProperties;
import com.princesses7.findy.shopping.product.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfdsBarcodeClient {

	@Qualifier("mfdsBarcodeRestClient")
	private final RestClient mfdsBarcodeRestClient;

	private final MfdsBarcodeProperties properties;

	public MfdsBarcodeResponse searchByBarcode(String barcode) {
		try {
			return mfdsBarcodeRestClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/{keyId}/{serviceId}/{dataType}/{startIdx}/{endIdx}/BRCD_NO={barcode}")
					.build(
						properties.keyId(),
						properties.serviceId(),
						properties.dataType(),
						1,
						10,
						barcode
					))
				.retrieve()
				.body(MfdsBarcodeResponse.class);
		} catch (UnknownContentTypeException exception) {
			throw new ProductException(BARCODE_PRODUCT_NOT_FOUND);
		} catch (RestClientException exception) {
			throw new ProductException(BARCODE_PRODUCT_NOT_FOUND);
		}
	}

	public String searchRawByBarcode(String barcode) {
		return mfdsBarcodeRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/{keyId}/{serviceId}/{dataType}/{startIdx}/{endIdx}/BRCD_NO={barcode}")
				.build(
					properties.keyId(),
					properties.serviceId(),
					properties.dataType(),
					1,
					10,
					barcode
				))
			.retrieve()
			.body(String.class);
	}
}