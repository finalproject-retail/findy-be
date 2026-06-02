package com.princesses7.findy.shopping.external.naver;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverProductMapper {

	private static final String EXTERNAL_SOURCE = "NAVER";

	private final OpenAiCategoryClassifierClient categoryClassifierClient;

	public ProductImportCommand toCommand(NaverShoppingItemResponse item) {
		String productName = cleanTitle(item.title());
		String brandName = resolveBrandName(item);
		String externalCategory = buildExternalCategory(item);

		ProductCategoryClassificationResponse classification =
			categoryClassifierClient.classify(productName, brandName, externalCategory);

		if (!classification.isClassified()) {
			throw new ProductException(CATEGORY_CLASSIFICATION_FAILED);
		}

		Integer salePrice = parsePrice(item.lprice());
		Integer originalPrice = resolveOriginalPrice(item.hprice(), salePrice);

		return new ProductImportCommand(
			classification.categoryId(),
			brandName,
			productName,
			null,
			EXTERNAL_SOURCE,
			item.productId(),
			originalPrice,
			null,
			item.image(),
			null,
			null,
			null,
			null,
			SaleStatus.ON_SALE,
			classification.confidence(),
			"AI",
			classification.reviewRequired()
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
			)
			.replaceAll("( > )+", " > ")
			.replaceAll("^ > | > $", "")
			.trim();
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

}