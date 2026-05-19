package com.princesses7.findy.shopping.external.mfds;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.global.config.MfdsBarcodeProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfdsBarcodeClient {

	private final RestClient mfdsBarcodeRestClient;
	private final MfdsBarcodeProperties properties;

	public MfdsBarcodeResponse searchByBarcode(String barcode) {
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
	}
}