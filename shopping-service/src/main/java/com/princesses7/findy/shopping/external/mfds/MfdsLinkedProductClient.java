package com.princesses7.findy.shopping.external.mfds;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.UnknownContentTypeException;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsLinkedProductItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsLinkedProductResponse;
import com.princesses7.findy.shopping.global.config.MfdsLinkedProductProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfdsLinkedProductClient {

	@Qualifier("mfdsLinkedProductRestClient")
	private final RestClient mfdsLinkedProductRestClient;

	private final MfdsLinkedProductProperties properties;

	public Optional<MfdsLinkedProductItemResponse> searchFirstByBarcode(String barcode) {
		try {
			MfdsLinkedProductResponse response = mfdsLinkedProductRestClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/{keyId}/{serviceId}/{dataType}/{startIdx}/{endIdx}/BAR_CD={barcode}")
					.build(
						properties.keyId(),
						properties.serviceId(),
						properties.dataType(),
						1,
						10,
						barcode
					))
				.retrieve()
				.body(MfdsLinkedProductResponse.class);

			if (response == null) {
				return Optional.empty();
			}

			return Optional.ofNullable(response.getFirstItemOrNull());
		} catch (UnknownContentTypeException exception) {
			return Optional.empty();
		} catch (RestClientException exception) {
			return Optional.empty();
		}
	}

	public String searchRawByBarcode(String barcode) {
		return mfdsLinkedProductRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/{keyId}/{serviceId}/{dataType}/{startIdx}/{endIdx}/BAR_CD={barcode}")
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