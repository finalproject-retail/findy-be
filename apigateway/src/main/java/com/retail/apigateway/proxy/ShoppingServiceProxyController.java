package com.retail.apigateway.proxy;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.retail.apigateway.config.ShoppingServiceProperties;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShoppingServiceProxyController {

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

	private static final List<String> SHOPPING_PATH_PREFIXES = List.of(
		"/api/v1/products",
		"/api/v1/carts",
		"/api/v1/shopping-lists",
		"/api/v1/search-keywords"
	);

	private final RestClient restClient;
	private final ShoppingServiceProperties shoppingServiceProperties;

	@RequestMapping({
		"/api/v1/products",
		"/api/v1/products/**",
		"/api/v1/carts",
		"/api/v1/carts/**",
		"/api/v1/shopping-lists",
		"/api/v1/shopping-lists/**",
		"/api/v1/search-keywords",
		"/api/v1/search-keywords/**"
	})
	public ResponseEntity<byte[]> forwardShoppingRequest(
		HttpServletRequest request,
		@RequestBody(required = false) byte[] body
	) {
		URI uri = buildTargetUri(request);
		HttpMethod method = HttpMethod.valueOf(request.getMethod());
		HttpHeaders headers = copyRequestHeaders(request);

		return restClient.method(method)
			.uri(uri)
			.headers(targetHeaders -> targetHeaders.addAll(headers))
			.body(body == null ? new byte[0] : body)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (req, res) -> {
				// 아래 toEntity에서 원본 status/body를 그대로 반환해야 하므로 예외 변환하지 않는다.
			})
			.toEntity(byte[].class);
	}

	private URI buildTargetUri(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		validateShoppingPath(requestUri);

		UriComponentsBuilder builder = UriComponentsBuilder
			.fromUriString(stripTrailingSlash(shoppingServiceProperties.baseUrl()))
			.path(requestUri);

		String queryString = request.getQueryString();
		if (queryString != null && !queryString.isBlank()) {
			builder.query(queryString);
		}

		return builder.build(true).toUri();
	}

	private void validateShoppingPath(String requestUri) {
		boolean matched = SHOPPING_PATH_PREFIXES.stream()
			.anyMatch(requestUri::startsWith);

		if (!matched) {
			throw new IllegalArgumentException("지원하지 않는 쇼핑 서비스 경로입니다: " + requestUri);
		}
	}

	private HttpHeaders copyRequestHeaders(HttpServletRequest request) {
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

	private String stripTrailingSlash(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalStateException("services.shopping.base-url 설정이 필요합니다.");
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
