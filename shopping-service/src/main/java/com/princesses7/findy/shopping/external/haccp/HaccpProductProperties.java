package com.princesses7.findy.shopping.external.haccp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.haccp")
public record HaccpProductProperties(
	String baseUrl,
	String serviceKey
) {
}