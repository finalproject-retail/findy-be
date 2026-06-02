package com.princesses7.findy.shopping.admin.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductDetailResponse;
import com.princesses7.findy.shopping.admin.product.dto.response.AdminProductPageResponse;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private InventoryRepository inventoryRepository;

	@InjectMocks
	private AdminProductService adminProductService;

	@Test
	@DisplayName("관리자 상품 목록 조회 시 검색어, 카테고리, 판매 상태 조건을 적용한다")
	void getProductsWithFilters() {
		when(productRepository.findAdminProductsWithKeyword(
			eq("우유"),
			eq(10L),
			eq(SaleStatus.ON_SALE),
			any(Pageable.class)
		)).thenReturn(Page.empty());

		AdminProductPageResponse response = adminProductService.getProducts(
			" 우유 ",
			10L,
			SaleStatus.ON_SALE,
			0,
			20,
			"createdAt",
			"desc"
		);

		assertThat(response).isNotNull();
		assertThat(response.products()).isEmpty();

		verify(productRepository).findAdminProductsWithKeyword(
			eq("우유"),
			eq(10L),
			eq(SaleStatus.ON_SALE),
			any(Pageable.class)
		);
	}

	@Test
	@DisplayName("관리자 상품 목록 조회 시 검색어가 공백이면 null 조건으로 조회한다")
	void getProductsWithBlankKeyword() {
		when(productRepository.findAdminProductsWithoutKeyword(
			isNull(),
			isNull(),
			any(Pageable.class)
		)).thenReturn(Page.empty());

		AdminProductPageResponse response = adminProductService.getProducts(
			" ",
			null,
			null,
			0,
			20,
			"createdAt",
			"desc"
		);

		assertThat(response).isNotNull();

		verify(productRepository).findAdminProductsWithoutKeyword(
			isNull(),
			isNull(),
			any(Pageable.class)
		);
	}

	@Test
	@DisplayName("관리자 상품 상세를 조회한다")
	void getProductDetail() {
		Product product = createProduct();

		when(productRepository.findByProductIdAndDeletedAtIsNull(1L))
			.thenReturn(Optional.of(product));
		when(inventoryRepository.findByProductProductIdAndStoreId(1L, 1L))
			.thenReturn(Optional.empty());

		AdminProductDetailResponse response = adminProductService.getProductDetail(1L);

		assertThat(response.productId()).isEqualTo(1L);
		assertThat(response.productName()).isEqualTo("서울우유 1L");
		assertThat(response.stockQuantity()).isNull();
	}

	@Test
	@DisplayName("관리자 상품 상세 조회 시 상품이 없으면 예외가 발생한다")
	void throwExceptionWhenProductNotFound() {
		when(productRepository.findByProductIdAndDeletedAtIsNull(999L))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminProductService.getProductDetail(999L))
			.isInstanceOf(ProductException.class)
			.hasMessage(PRODUCT_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("관리자 상품 목록 조회 시 페이지 요청 값이 올바르지 않으면 예외가 발생한다")
	void throwExceptionWhenInvalidPageRequest() {
		assertThatThrownBy(() -> adminProductService.getProducts(
			null,
			null,
			null,
			0,
			101,
			"createdAt",
			"desc"
		))
			.isInstanceOf(ProductException.class)
			.hasMessage(INVALID_INPUT_VALUE.getMessage());
	}

	@Test
	@DisplayName("관리자 상품 목록 조회 시 지원하지 않는 정렬 기준이면 예외가 발생한다")
	void throwExceptionWhenInvalidSortType() {
		assertThatThrownBy(() -> adminProductService.getProducts(
			null,
			null,
			null,
			0,
			20,
			"invalidSort",
			"desc"
		))
			.isInstanceOf(ProductException.class)
			.hasMessage(INVALID_SORT_TYPE.getMessage());
	}

	private Product createProduct() {
		Product product = Product.create(new ProductImportCommand(
			10L,
			"서울우유",
			"서울우유 1L",
			"8800000000000",
			"NAVER",
			"naver-product-1",
			3000,
			"관리자 상품 상세 조회 테스트 상품",
			null,
			null,
			null,
			null,
			null,
			SaleStatus.ON_SALE,
			new BigDecimal("0.95"),
			"AI",
			false
		));

		ReflectionTestUtils.setField(product, "productId", 1L);

		return product;
	}
}