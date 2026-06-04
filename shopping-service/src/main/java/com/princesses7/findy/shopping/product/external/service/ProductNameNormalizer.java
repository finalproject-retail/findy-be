package com.princesses7.findy.shopping.product.external.service;

import org.springframework.stereotype.Component;

@Component
public class ProductNameNormalizer {

	public String normalize(String value) {
		if (value == null) {
			return "";
		}

		return value
			.toLowerCase()
			.replace("시드_", "")
			.replaceAll("\\([^)]*\\)", "")
			.replaceAll("\\[[^]]*\\]", "")
			.replaceAll("[^가-힣a-z0-9]", "");
	}
}