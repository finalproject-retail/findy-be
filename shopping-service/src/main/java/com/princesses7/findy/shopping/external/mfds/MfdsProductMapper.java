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

	public ProductImportCommand toCommand(MfdsBarcodeItemResponse item) {
		return new ProductImportCommand(
			DEFAULT_CATEGORY_ID,
			item.BSSH_NM(),
			item.PRDLST_NM(),
			item.BAR_CD(),
			EXTERNAL_SOURCE,
			item.PRDLST_REPORT_NO(),
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
			"DEFAULT",
			true
		);
	}

	private String createDescription(MfdsBarcodeItemResponse item) {
		return """
			식품 유형: %s
			업종: %s
			소비기한: %s
			제조사 주소: %s
			""".formatted(
			item.PRDLST_DCNM(),
			item.INDUTY_NM(),
			item.POG_DAYCNT(),
			item.SITE_ADDR()
		).trim();
	}
}