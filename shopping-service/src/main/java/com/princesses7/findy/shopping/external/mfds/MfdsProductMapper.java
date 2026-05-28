package com.princesses7.findy.shopping.external.mfds;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsLinkedProductItemResponse;
import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MfdsProductMapper {

	private static final String EXTERNAL_SOURCE = "MFDS";
	private static final int DEFAULT_PRICE = 0;

	private final OpenAiCategoryClassifierClient categoryClassifierClient;

	public ProductImportCommand toCommand(
		String barcode,
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		String productName = getProductName(barcodeItem, linkedItem);
		String brandName = getCompanyName(barcodeItem, linkedItem);
		String externalCategory = createExternalCategory(barcodeItem, linkedItem);

		ProductCategoryClassificationResponse classification =
			categoryClassifierClient.classify(productName, brandName, externalCategory);

		if (!classification.isClassified()) {
			throw new ProductException(CATEGORY_CLASSIFICATION_FAILED);
		}

		return new ProductImportCommand(
			classification.categoryId(),
			brandName,
			productName,
			getBarcode(barcode, barcodeItem, linkedItem),
			EXTERNAL_SOURCE,
			getReportNo(barcodeItem, linkedItem),
			DEFAULT_PRICE,
			DEFAULT_PRICE,
			BigDecimal.ZERO,
			createDescription(barcodeItem, linkedItem),
			null,
			null,
			null,
			null,
			null,
			createBadgeText(barcodeItem, linkedItem),
			SaleStatus.ON_SALE,
			classification.confidence(),
			"AI",
			classification.reviewRequired()
		);
	}

	private String createExternalCategory(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		String barcodeCategory = barcodeItem
			.map(item -> String.join(" > ",
				defaultText(item.categoryLarge()),
				defaultText(item.categoryMiddle()),
				defaultText(item.categorySmall())
			))
			.orElse("");

		String foodType = linkedItem
			.map(MfdsLinkedProductItemResponse::foodType)
			.filter(this::hasText)
			.orElse("");

		return String.join(" > ", barcodeCategory, foodType)
			.replace("정보 없음", "")
			.replaceAll("( > )+", " > ")
			.replaceAll("^ > | > $", "")
			.trim();
	}

	private String getCompanyName(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return barcodeItem
			.map(MfdsBarcodeItemResponse::companyName)
			.filter(this::hasText)
			.orElseGet(() -> linkedItem
				.map(MfdsLinkedProductItemResponse::companyName)
				.filter(this::hasText)
				.orElse("정보 없음"));
	}

	private String getProductName(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return barcodeItem
			.map(MfdsBarcodeItemResponse::productName)
			.filter(this::hasText)
			.orElseGet(() -> linkedItem
				.map(MfdsLinkedProductItemResponse::productName)
				.filter(this::hasText)
				.orElse("식약처 연동 상품"));
	}

	private String getBarcode(
		String barcode,
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return barcodeItem
			.map(MfdsBarcodeItemResponse::barcode)
			.filter(this::hasText)
			.orElseGet(() -> linkedItem
				.map(MfdsLinkedProductItemResponse::barcode)
				.filter(this::hasText)
				.orElse(barcode));
	}

	private String getReportNo(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return barcodeItem
			.map(MfdsBarcodeItemResponse::reportNo)
			.filter(this::hasText)
			.orElseGet(() -> linkedItem
				.map(MfdsLinkedProductItemResponse::reportNo)
				.filter(this::hasText)
				.orElse(null));
	}

	private String createDescription(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		StringBuilder description = new StringBuilder();

		barcodeItem.ifPresent(item -> description.append("""
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
		).trim()));

		linkedItem.ifPresent(item -> {
			if (!description.isEmpty()) {
				description.append("\n\n");
			}

			description.append("""
				[바코드연계제품정보]
				상품명: %s
				제조사: %s
				바코드: %s
				품목보고번호: %s
				식품유형: %s
				업종: %s
				소비기한: %s
				제조사 주소: %s
				품목허가일자: %s
				영업종료일자: %s
				""".formatted(
				defaultText(item.productName()),
				defaultText(item.companyName()),
				defaultText(item.barcode()),
				defaultText(item.reportNo()),
				defaultText(item.foodType()),
				defaultText(item.businessType()),
				defaultText(item.expirationPeriod()),
				defaultText(item.siteAddress()),
				defaultText(item.permissionDate()),
				defaultText(item.endDate())
			).trim());
		});

		if (description.isEmpty()) {
			return "식약처 연동 상품";
		}

		return description.toString();
	}

	private String createBadgeText(
		Optional<MfdsBarcodeItemResponse> barcodeItem,
		Optional<MfdsLinkedProductItemResponse> linkedItem
	) {
		return barcodeItem
			.map(MfdsBarcodeItemResponse::categorySmall)
			.filter(this::hasText)
			.orElseGet(() -> linkedItem
				.map(MfdsLinkedProductItemResponse::foodType)
				.filter(this::hasText)
				.orElse("식약처 연동"));
	}

	private String defaultText(String value) {
		if (!hasText(value)) {
			return "정보 없음";
		}

		return value;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}