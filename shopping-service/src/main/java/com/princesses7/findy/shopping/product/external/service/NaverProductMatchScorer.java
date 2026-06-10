package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchedBy;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverProductMatchScorer {

	private final ProductNameNormalizer normalizer;

	public ProductMatchResult score(Product product, NaverShoppingItemResponse item) {
		String internalName = normalizer.normalize(product.getProductName());
		String externalName = normalizer.normalize(cleanTitle(item.title()));

		double score = calculateNameScore(internalName, externalName);
		score += calculateBrandBonus(product.getBrandName(), resolveBrandName(item));
		score += calculateVolumeBonus(product.getVolume(), cleanTitle(item.title()));

		BigDecimal confidence = BigDecimal.valueOf(Math.min(score, 1.0))
			.setScale(4, RoundingMode.HALF_UP);

		return new ProductMatchResult(
			confidence,
			ProductExternalMatchedBy.RULE,
			"Naver 상품명, 브랜드, 용량 기반 rule 매칭 결과입니다."
		);
	}

	public String cleanTitle(String title) {
		if (title == null) {
			return "";
		}

		return title
			.replaceAll("<[^>]*>", "")
			.replace("&quot;", "\"")
			.replace("&amp;", "&")
			.trim();
	}

	private String resolveBrandName(NaverShoppingItemResponse item) {
		if (item.brand() != null && !item.brand().isBlank()) {
			return item.brand();
		}

		if (item.maker() != null && !item.maker().isBlank()) {
			return item.maker();
		}

		return null;
	}

	private double calculateNameScore(String internalName, String externalName) {
		if (internalName.isBlank() || externalName.isBlank()) {
			return 0;
		}

		if (internalName.equals(externalName)) {
			return 0.85;
		}

		if (internalName.contains(externalName) || externalName.contains(internalName)) {
			return 0.75;
		}

		return calculateLongestCommonRatio(internalName, externalName) * 0.70;
	}

	private double calculateBrandBonus(String internalBrandName, String externalBrandName) {
		String internalBrand = normalizer.normalize(internalBrandName);
		String externalBrand = normalizer.normalize(externalBrandName);

		if (internalBrand.isBlank() || externalBrand.isBlank()) {
			return 0;
		}

		if (internalBrand.equals(externalBrand)
			|| internalBrand.contains(externalBrand)
			|| externalBrand.contains(internalBrand)) {
			return 0.10;
		}

		return 0;
	}

	private double calculateVolumeBonus(String internalVolume, String externalProductName) {
		String volume = normalizer.normalize(internalVolume);
		String externalName = normalizer.normalize(externalProductName);

		if (volume.isBlank() || externalName.isBlank()) {
			return 0;
		}

		if (externalName.contains(volume)) {
			return 0.05;
		}

		return 0;
	}

	private double calculateLongestCommonRatio(String first, String second) {
		int longest = longestCommonSubstringLength(first, second);
		int maxLength = Math.max(first.length(), second.length());

		if (maxLength == 0) {
			return 0;
		}

		return (double)longest / maxLength;
	}

	private int longestCommonSubstringLength(String first, String second) {
		int[][] dp = new int[first.length() + 1][second.length() + 1];
		int max = 0;

		for (int i = 1; i <= first.length(); i++) {
			for (int j = 1; j <= second.length(); j++) {
				if (first.charAt(i - 1) == second.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1] + 1;
					max = Math.max(max, dp[i][j]);
				}
			}
		}

		return max;
	}
}
