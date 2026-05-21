package com.princesses7.findy.shopping.external.mfds;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

@Component
public class MfdsProductMapper {

	private static final String EXTERNAL_SOURCE = "MFDS";
	private static final Long DEFAULT_CATEGORY_ID = 1L;
	private static final int DEFAULT_PRICE = 0;
	private static final String DEFAULT_CATEGORY_CLASSIFIED_BY = "MFDS";

	public ProductImportCommand toCommand(MfdsBarcodeItemResponse item) {
		return new ProductImportCommand(
			DEFAULT_CATEGORY_ID,
			item.companyName(),
			item.productName(),
			item.barcode(),
			EXTERNAL_SOURCE,
			item.reportNo(),
			DEFAULT_PRICE,
			DEFAULT_PRICE,
			BigDecimal.ZERO,
			createDescription(item),
			null,
			null,
			null,
			null,
			null,
			null,
			SaleStatus.ON_SALE,
			BigDecimal.ZERO,
			DEFAULT_CATEGORY_CLASSIFIED_BY,
			true
		);
	}

	private String createDescription(MfdsBarcodeItemResponse item) {
		return """
			식약처 유통바코드 연동 상품
			대분류: %s
			중분류: %s
			소분류: %s
			품목보고번호: %s
			최종수정일시: %s
			""".formatted(
			defaultText(item.categoryLarge()),
			defaultText(item.categoryMiddle()),
			defaultText(item.categorySmall()),
			defaultText(item.reportNo()),
			defaultText(item.lastUpdatedAt())
		).trim();
	}

	private String defaultText(String value) {
		if (value == null || value.isBlank()) {
			return "정보 없음";
		}

		return value;
	}
}