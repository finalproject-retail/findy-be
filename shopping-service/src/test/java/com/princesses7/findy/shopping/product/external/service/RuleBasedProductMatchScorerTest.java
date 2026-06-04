package com.princesses7.findy.shopping.product.external.service;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

class RuleBasedProductMatchScorerTest {

	private final RuleBasedProductMatchScorer scorer =
		new RuleBasedProductMatchScorer(new ProductNameNormalizer());

	@Test
	@DisplayName("상품명과 브랜드가 유사하면 높은 매칭 점수를 반환한다")
	void scoreHighWhenProductNameAndBrandAreSimilar() {
		Product product = Product.create(new ProductImportCommand(
			1L,
			"농심",
			"시드_신라면 120g",
			null,
			null,
			null,
			0,
			null,
			null,
			"EA",
			"120g",
			null,
			null,
			SaleStatus.ON_SALE,
			null,
			null,
			false
		));

		KcaProductPriceItemResponse externalProduct = new KcaProductPriceItemResponse(
			"20260522",
			"1000",
			"신라면 120g",
			"100",
			null,
			null,
			"농심",
			"1500",
			null,
			null,
			null,
			null,
			null,
			"2026-05-29 14:10:38"
		);

		ProductMatchResult result = scorer.score(product, externalProduct);

		assertThat(result.confidence()).isGreaterThanOrEqualTo(new BigDecimal("0.8500"));
	}

	@Test
	@DisplayName("서로 다른 상품이면 낮은 매칭 점수를 반환한다")
	void scoreLowWhenProductNameIsDifferent() {
		Product product = Product.create(new ProductImportCommand(
			1L,
			"농심",
			"시드_신라면 120g",
			null,
			null,
			null,
			0,
			null,
			null,
			"EA",
			"120g",
			null,
			null,
			SaleStatus.ON_SALE,
			null,
			null,
			false
		));

		KcaProductPriceItemResponse externalProduct = new KcaProductPriceItemResponse(
			"20260522",
			"2000",
			"코카콜라 제로 500ml",
			"100",
			null,
			null,
			"코카콜라",
			"2000",
			null,
			null,
			null,
			null,
			null,
			"2026-05-29 14:10:38"
		);

		ProductMatchResult result = scorer.score(product, externalProduct);

		assertThat(result.confidence()).isLessThan(new BigDecimal("0.5500"));
	}
}