package com.princesses7.findy.shopping.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductPageResponse;
import com.princesses7.findy.shopping.product.repository.ProductRepository;
import com.princesses7.findy.shopping.search.service.SearchKeywordRankingService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private InventoryRepository inventoryRepository;

	@Mock
	private SearchKeywordRankingService searchKeywordRankingService;

	@Mock
	private ProductRankingService productRankingService;

	@Mock
	private ShoppingAnalyticsEventService shoppingAnalyticsEventService;

	@InjectMocks
	private ProductService productService;

	@Test
	@DisplayName("상품 검색 시 keyword 조건을 적용하고 검색어를 기록한다")
	void getProductsWithKeyword() {
		when(productRepository.findByProductNameContainingIgnoreCaseAndDeletedAtIsNull(
			eq("우유"),
			any(Pageable.class)
		)).thenReturn(Page.empty());

		ProductPageResponse response = productService.getProducts(
			null,
			" 우유 ",
			0,
			10,
			"createdAt",
			"desc",
			1L
		);

		assertThat(response).isNotNull();

		verify(searchKeywordRankingService).record("우유");
		verify(productRepository).findByProductNameContainingIgnoreCaseAndDeletedAtIsNull(
			eq("우유"),
			any(Pageable.class)
		);
	}

	@Test
	@DisplayName("카테고리와 keyword가 함께 있으면 카테고리 내 상품명 검색을 수행한다")
	void getProductsWithCategoryAndKeyword() {
		when(productRepository.findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNull(
			eq(10L),
			eq("우유"),
			any(Pageable.class)
		)).thenReturn(Page.empty());

		ProductPageResponse response = productService.getProducts(
			10L,
			"우유",
			0,
			10,
			"createdAt",
			"desc",
			1L
		);

		assertThat(response).isNotNull();

		verify(searchKeywordRankingService).record("우유");
		verify(productRepository).findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNull(
			eq(10L),
			eq("우유"),
			any(Pageable.class)
		);
	}

	@Test
	@DisplayName("keyword가 없으면 검색어를 기록하지 않고 전체 상품을 조회한다")
	void getProductsWithoutKeyword() {
		when(productRepository.findByDeletedAtIsNull(any(Pageable.class)))
			.thenReturn(Page.empty());

		ProductPageResponse response = productService.getProducts(
			null,
			" ",
			0,
			10,
			"createdAt",
			"desc",
			1L
		);

		assertThat(response).isNotNull();

		verify(searchKeywordRankingService, never()).record(anyString());
		verify(productRepository).findByDeletedAtIsNull(any(Pageable.class));
	}

	@Test
	@DisplayName("categoryId만 있으면 카테고리 기준으로 상품을 조회한다")
	void getProductsWithCategoryOnly() {
		when(productRepository.findByCategoryIdAndDeletedAtIsNull(
			eq(10L),
			any(Pageable.class)
		)).thenReturn(Page.empty());

		ProductPageResponse response = productService.getProducts(
			10L,
			null,
			0,
			10,
			"createdAt",
			"desc",
			1L
		);

		assertThat(response).isNotNull();

		verify(searchKeywordRankingService, never()).record(anyString());
		verify(productRepository).findByCategoryIdAndDeletedAtIsNull(
			eq(10L),
			any(Pageable.class)
		);
	}
}