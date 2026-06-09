package com.princesses7.findy.shopping.external.haccp;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;
import com.princesses7.findy.shopping.external.haccp.support.HaccpCompanyNameParser;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

@Component
public class HaccpProductMapper {

	private static final String EXTERNAL_SOURCE = "HACCP";
	private static final int DEFAULT_PRICE = 0;
	private static final String CATEGORY_CLASSIFIED_BY_OPENAI = "OPENAI";
	private static final String CATEGORY_CLASSIFIED_BY_NONE = "NONE";

	public ProductImportCommand toEnrichmentCommand(HaccpProductItemResponse item) {
		return new ProductImportCommand(
			null,
			extractBrandName(item),
			clean(item.productName()),
			toNullableInfo(item.barcode()),
			EXTERNAL_SOURCE,
			clean(item.productReportNo()),
			DEFAULT_PRICE,
			createDescription(item),
			getImageUrl(item),
			"1개",
			toNullableInfo(item.capacity()),
			toNullableAllergy(item.allergy()),
			toNullableInfo(item.productKind()),
			SaleStatus.ON_SALE,
			null,
			CATEGORY_CLASSIFIED_BY_NONE,
			null
		);
	}

	public ProductImportCommand toCreateCommand(
		HaccpProductItemResponse item,
		ProductCategoryClassificationResponse classification
	) {
		return new ProductImportCommand(
			classification.categoryId(),
			extractBrandName(item),
			clean(item.productName()),
			toNullableInfo(item.barcode()),
			EXTERNAL_SOURCE,
			clean(item.productReportNo()),
			DEFAULT_PRICE,
			createDescription(item),
			getImageUrl(item),
			"1개",
			toNullableInfo(item.capacity()),
			toNullableAllergy(item.allergy()),
			toNullableInfo(item.productKind()),
			SaleStatus.ON_SALE,
			classification.confidence(),
			CATEGORY_CLASSIFIED_BY_OPENAI,
			classification.reviewRequired()
		);
	}

	public String extractBrandName(HaccpProductItemResponse item) {
		return toNullableInfo(
			HaccpCompanyNameParser.extractBrandName(
				item.seller(),
				item.manufacture()
			)
		);
	}

	private String createDescription(HaccpProductItemResponse item) {
		StringBuilder description = new StringBuilder();

		appendLine(description, "HACCP 제품이미지 및 포장지표기정보 연동 상품");
		appendLine(description, "품목보고번호: " + defaultText(item.productReportNo()));
		appendLine(description, "식품유형: " + defaultText(item.productKind()));
		appendLine(description, "원재료: " + defaultText(item.rawMaterial()));
		appendLine(description, "영양성분: " + defaultText(item.nutrient()));
		appendLine(description, "제조사: " + defaultText(item.manufacture()));
		appendLine(description, "판매원: " + defaultText(item.seller()));

		return description.toString().trim();
	}

	private void appendLine(StringBuilder builder, String value) {
		if (!StringUtils.hasText(value)) {
			return;
		}

		if (!builder.isEmpty()) {
			builder.append("\n");
		}

		builder.append(value);
	}

	private String getImageUrl(HaccpProductItemResponse item) {
		return firstText(item.productImageUrl(), item.metaImageUrl());
	}

	private String toNullableAllergy(String value) {
		String cleanedValue = clean(value);

		if (!StringUtils.hasText(cleanedValue)) {
			return null;
		}

		if ("알수없음".equals(cleanedValue) || "알 수 없음".equals(cleanedValue)) {
			return null;
		}

		return cleanedValue;
	}

	private String toNullableInfo(String value) {
		String cleanedValue = clean(value);

		if (!StringUtils.hasText(cleanedValue)) {
			return null;
		}

		if (isUnknownValue(cleanedValue)) {
			return null;
		}

		return cleanedValue;
	}

	private boolean isUnknownValue(String value) {
		return "알수없음".equals(value)
			|| "알 수 없음".equals(value)
			|| "UNKNOWN".equalsIgnoreCase(value)
			|| "_".equals(value)
			|| "-".equals(value)
			|| ".".equals(value)
			|| ":".equals(value)
			|| "：".equals(value)
			|| "/".equals(value)
			|| ",".equals(value);
	}

	private String firstText(String first, String second) {
		if (StringUtils.hasText(clean(first))) {
			return clean(first);
		}

		if (StringUtils.hasText(clean(second))) {
			return clean(second);
		}

		return null;
	}

	private String defaultText(String value) {
		String cleanedValue = clean(value);

		if (!StringUtils.hasText(cleanedValue)) {
			return "정보 없음";
		}

		return cleanedValue;
	}

	private String clean(String value) {
		if (value == null) {
			return null;
		}

		return value.trim();
	}
}