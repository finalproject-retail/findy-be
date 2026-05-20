package com.princesses7.findy.shopping.external.naver;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverProductMapper {

	private static final String EXTERNAL_SOURCE = "NAVER";
	private static final Long DEFAULT_CATEGORY_ID = 1L;

	private final OpenAiCategoryClassifierClient categoryClassifierClient;

	public ProductImportCommand toCommand(NaverShoppingItemResponse item) {
		String productName = cleanTitle(item.title());
		String brandName = resolveBrandName(item);
		String externalCategory = buildExternalCategory(item);

		ProductCategoryClassificationResponse classification =
			categoryClassifierClient.classify(productName, brandName, externalCategory);

		Long categoryId = classification.categoryId() == null
			? DEFAULT_CATEGORY_ID
			: classification.categoryId();

		Integer salePrice = parsePrice(item.lprice());
		Integer originalPrice = resolveOriginalPrice(item.hprice(), salePrice);

		return new ProductImportCommand(
			categoryId,
			brandName,
			productName,
			null,
			EXTERNAL_SOURCE,
			item.productId(),
			originalPrice,
			salePrice,
			calculateDiscountRate(originalPrice, salePrice),
			null,
			item.image(),
			null,
			null,
			null,
			null,
			null,
			SaleStatus.ON_SALE
		);
	}

	private String cleanTitle(String title) {
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

	private String buildExternalCategory(NaverShoppingItemResponse item) {
		return String.join(" > ",
			nullToEmpty(item.category1()),
			nullToEmpty(item.category2()),
			nullToEmpty(item.category3()),
			nullToEmpty(item.category4())
		).replaceAll("( > )+", " > ").trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private Integer parsePrice(String price) {
		if (price == null || price.isBlank()) {
			return 0;
		}

		return Integer.parseInt(price);
	}

	private Integer resolveOriginalPrice(String hprice, Integer salePrice) {
		int highPrice = parsePrice(hprice);

		if (highPrice > salePrice) {
			return highPrice;
		}

		return salePrice;
	}

	private BigDecimal calculateDiscountRate(Integer originalPrice, Integer salePrice) {
		if (originalPrice == null || originalPrice == 0 || salePrice == null) {
			return BigDecimal.ZERO;
		}

		if (originalPrice <= salePrice) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(originalPrice - salePrice)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(originalPrice), 2, java.math.RoundingMode.HALF_UP);
	}
}