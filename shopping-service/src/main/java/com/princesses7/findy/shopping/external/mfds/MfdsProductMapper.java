package com.princesses7.findy.shopping.external.mfds;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsLinkedProductItemResponse;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

@Component
public class MfdsProductMapper {

	private static final String EXTERNAL_SOURCE = "MFDS";
	private static final Long DEFAULT_CATEGORY_ID = 1L;
	private static final int DEFAULT_PRICE = 0;
	private static final String CATEGORY_CLASSIFIED_BY = "MFDS";

	public ProductImportCommand toCommand(
		MfdsBarcodeItemResponse barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return new ProductImportCommand(
			DEFAULT_CATEGORY_ID,
			barcodeItem.companyName(),
			barcodeItem.productName(),
			barcodeItem.barcode(),
			EXTERNAL_SOURCE,
			barcodeItem.reportNo(),
			DEFAULT_PRICE,
			DEFAULT_PRICE,
			BigDecimal.ZERO,
			createDescription(barcodeItem, linkedItem),
			null,
			null,
			null,
			null,
			null,
			createBadgeText(barcodeItem),
			SaleStatus.ON_SALE,
			BigDecimal.ZERO,
			CATEGORY_CLASSIFIED_BY,
			true
		);
	}

	private String createDescription(
		MfdsBarcodeItemResponse barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		StringBuilder description = new StringBuilder();

		description.append("""
			식약처 유통바코드 연동 상품
			대분류: %s
			중분류: %s
			소분류: %s
			품목보고번호: %s
			최종수정일시: %s
			""".formatted(
			defaultText(barcodeItem.categoryLarge()),
			defaultText(barcodeItem.categoryMiddle()),
			defaultText(barcodeItem.categorySmall()),
			defaultText(barcodeItem.reportNo()),
			defaultText(barcodeItem.lastUpdatedAt())
		).trim());

		linkedItem.ifPresent(item -> description.append("\n\n")
			.append("""
				[바코드연계제품정보]
				식품유형: %s
				업종: %s
				소비기한: %s
				제조사 주소: %s
				품목허가일자: %s
				영업종료일자: %s
				""".formatted(
				defaultText(item.foodType()),
				defaultText(item.businessType()),
				defaultText(item.expirationPeriod()),
				defaultText(item.siteAddress()),
				defaultText(item.permissionDate()),
				defaultText(item.endDate())
			).trim()));

		return description.toString();
	}

	private String createBadgeText(MfdsBarcodeItemResponse barcodeItem) {
		if (barcodeItem.categorySmall() == null || barcodeItem.categorySmall().isBlank()) {
			return "식약처 연동";
		}

		return barcodeItem.categorySmall();
	}

	private String defaultText(String value) {
		if (value == null || value.isBlank()) {
			return "정보 없음";
		}

		return value;
	}
}