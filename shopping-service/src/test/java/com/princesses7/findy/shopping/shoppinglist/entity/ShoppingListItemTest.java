package com.princesses7.findy.shopping.shoppinglist.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;
import com.princesses7.findy.shopping.shoppinglist.type.ShoppingListItemType;

class ShoppingListItemTest {

	@Test
	@DisplayName("카테고리 항목은 상품 ID 없이 생성할 수 있다")
	void createCategoryItemWithoutProductId() {
		ShoppingListItem item = ShoppingListItem.createFromCategory(
			null,
			10L,
			"우유/요거트",
			2
		);

		assertThat(item.getItemType()).isEqualTo(ShoppingListItemType.CATEGORY);
		assertThat(item.getProductId()).isNull();
		assertThat(item.getCategoryId()).isEqualTo(10L);
		assertThat(item.getCategoryName()).isEqualTo("우유/요거트");
		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getCompletedQuantity()).isZero();
	}

	@Test
	@DisplayName("카테고리 항목은 체크 상태로 완료 수량을 계산한다")
	void changeCategoryItemChecked() {
		ShoppingListItem item = ShoppingListItem.createFromCategory(
			null,
			1L,
			"과일",
			3
		);

		item.changeChecked(true);

		assertThat(item.isChecked()).isTrue();
		assertThat(item.getScanStatus()).isEqualTo(ScanStatus.SCANNED);
		assertThat(item.getCompletedQuantity()).isEqualTo(3);
	}

	@Test
	@DisplayName("새 수량이 스캔 수량 이상이면 바코드 없이 수량을 감소할 수 있다")
	void decreaseQuantityWithoutScanWhenNewQuantityIsGreaterThanOrEqualToScannedQuantity() {
		ShoppingListItem item = ShoppingListItem.createFromCartItem(
			null,
			CartItem.create(null, 10001L, 3)
		);

		item.completeScan();
		item.completeScan();

		assertThat(item.getQuantity()).isEqualTo(3);
		assertThat(item.getScannedQuantity()).isEqualTo(2);
		assertThat(item.requiresScanToDecrease(2)).isFalse();

		item.changeQuantity(2);

		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getScannedQuantity()).isEqualTo(2);
		assertThat(item.getScanStatus()).isEqualTo(ScanStatus.SCANNED);
		assertThat(item.isChecked()).isTrue();
	}

	@Test
	@DisplayName("새 수량이 스캔 수량보다 작으면 바코드 스캔이 필요하다")
	void requireScanWhenNewQuantityIsLessThanScannedQuantity() {
		ShoppingListItem item = ShoppingListItem.createFromCartItem(
			null,
			CartItem.create(null, 10001L, 3)
		);

		item.completeScan();
		item.completeScan();
		item.completeScan();

		assertThat(item.getQuantity()).isEqualTo(3);
		assertThat(item.getScannedQuantity()).isEqualTo(3);
		assertThat(item.requiresScanToDecrease(2)).isTrue();
	}

	@Test
	@DisplayName("카테고리 항목 수량 감소는 바코드 스캔을 요구하지 않는다")
	void decreaseCategoryItemQuantityWithoutScan() {
		ShoppingListItem item = ShoppingListItem.createFromCategory(
			null,
			1L,
			"과일",
			3
		);

		assertThat(item.requiresScanToDecrease(2)).isFalse();

		item.changeQuantity(2);

		assertThat(item.getQuantity()).isEqualTo(2);
		assertThat(item.getScanStatus()).isEqualTo(ScanStatus.NOT_SCANNED);
	}
}