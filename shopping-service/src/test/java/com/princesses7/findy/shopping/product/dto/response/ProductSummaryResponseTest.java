package com.princesses7.findy.shopping.product.dto.response;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

class ProductSummaryResponseTest {

	@Test
	@DisplayName("상품 판매 상태가 품절이면 재고가 있어도 품절 상태로 응답한다")
	void fromReturnsOutOfStockWhenSaleStatusIsOutOfStock() {
		Product product = product(SaleStatus.OUT_OF_STOCK);
		Inventory inventory = Inventory.createDefault(product, 1L, 20);

		ProductSummaryResponse response = ProductSummaryResponse.from(product, inventory);

		assertThat(response.stockQuantity()).isEqualTo(20);
		assertThat(response.stockStatus()).isEqualTo("OUT_OF_STOCK");
		assertThat(response.stockBadgeText()).isEqualTo("품절");
		assertThat(response.isPurchasable()).isFalse();
	}

	@Test
	@DisplayName("재고가 없으면 구매 불가로 판단한다")
	void isPurchasableReturnsFalseWhenInventoryIsMissing() {
		ProductSummaryResponse response = ProductSummaryResponse.from(product(SaleStatus.ON_SALE), null);

		assertThat(response.stockQuantity()).isNull();
		assertThat(response.stockStatus()).isNull();
		assertThat(response.stockBadgeText()).isEqualTo("재고 확인 불가");
		assertThat(response.isPurchasable()).isFalse();
		assertThat(response.purchasableQuantity()).isZero();
	}

	private Product product(SaleStatus saleStatus) {
		return Product.create(new ProductImportCommand(
			1L,
			"브랜드",
			"상품",
			"barcode",
			"SEED",
			"SEED-1",
			1000,
			null,
			null,
			"1개",
			"100g",
			null,
			null,
			saleStatus,
			null,
			"SEED",
			false
		));
	}
}