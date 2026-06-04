package com.princesses7.findy.shopping.shoppinglist.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}