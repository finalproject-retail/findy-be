package com.princesses7.findy.shopping.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mfds.linked-product")
public record MfdsLinkedProductProperties(
	String baseUrl,
	String keyId,
	String serviceId,
	String dataType
) {
}