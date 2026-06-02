package com.retail.apigateway.proxy;

import java.util.Set;

import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;

public final class ProxyHeaderUtils {

	private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
		"connection",
		"keep-alive",
		"proxy-authenticate",
		"proxy-authorization",
		"te",
		"trailer",
		"transfer-encoding",
		"upgrade",
		"host",
		"content-length"
	);

	private ProxyHeaderUtils() {
	}

	public static HttpHeaders copyRequestHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
			if (HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
				return;
			}
			request.getHeaders(headerName).asIterator()
				.forEachRemaining(headerValue -> headers.add(headerName, headerValue));
		});
		return headers;
	}
}
