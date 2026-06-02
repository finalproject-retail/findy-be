package com.retail.apigateway.proxy;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GatewayProxyController {

	private final RestClient restClient;
	private final ServiceRouteResolver serviceRouteResolver;

	@RequestMapping("/api/**")
	public ResponseEntity<byte[]> forwardApiRequest(
		HttpServletRequest request,
		@RequestBody(required = false) byte[] body
	) {
		URI uri = buildTargetUri(request);
		HttpMethod method = HttpMethod.valueOf(request.getMethod());
		HttpHeaders headers = ProxyHeaderUtils.copyRequestHeaders(request);

		return restClient.method(method)
			.uri(uri)
			.headers(targetHeaders -> targetHeaders.addAll(headers))
			.body(body == null ? new byte[0] : body)
			.retrieve()
			.onStatus(HttpStatusCode::isError, (requestSpec, responseSpec) -> {
			})
			.toEntity(byte[].class);
	}

	private URI buildTargetUri(HttpServletRequest request) {
		UriComponentsBuilder builder = UriComponentsBuilder
			.fromUriString(serviceRouteResolver.resolveBaseUrl(request.getRequestURI()))
			.path(request.getRequestURI());

		String queryString = request.getQueryString();
		if (queryString != null && !queryString.isBlank()) {
			builder.query(queryString);
		}

		return builder.build(true).toUri();
	}
}
