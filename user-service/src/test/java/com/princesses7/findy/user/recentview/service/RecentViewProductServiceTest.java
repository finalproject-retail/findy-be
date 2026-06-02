package com.princesses7.findy.user.recentview.service;

import static com.princesses7.findy.user.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.recentview.client.ShoppingProductClient;
import com.princesses7.findy.user.recentview.dto.request.AddRecentViewProductRequest;
import com.princesses7.findy.user.recentview.dto.response.ProductSummaryResponse;
import com.princesses7.findy.user.recentview.dto.response.RecentViewProductListResponse;
import com.princesses7.findy.user.recentview.entity.RecentViewProduct;
import com.princesses7.findy.user.recentview.repository.RecentViewProductRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RecentViewProductServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RecentViewProductRepository recentViewProductRepository;

	@Mock
	private ShoppingProductClient shoppingProductClient;

	@InjectMocks
	private RecentViewProductService recentViewProductService;

	@Test
	@DisplayName("최근 본 상품 추가 시 기존 이력이 없으면 새 이력을 저장한다")
	void addRecentViewProductWhenNewProduct() {
		AddRecentViewProductRequest request = new AddRecentViewProductRequest(10001L);

		when(userRepository.existsById(1L)).thenReturn(true);
		when(recentViewProductRepository.findByUserIdAndProductId(1L, 10001L))
			.thenReturn(Optional.empty());

		recentViewProductService.addRecentViewProduct(1L, request);

		verify(recentViewProductRepository).save(argThat(recentViewProduct ->
			recentViewProduct.getUserId().equals(1L)
				&& recentViewProduct.getProductId().equals(10001L)
				&& recentViewProduct.getViewedAt() != null
		));
	}

	@Test
	@DisplayName("최근 본 상품 추가 시 기존 이력이 있으면 조회 시각을 갱신한다")
	void refreshRecentViewProductWhenAlreadyExists() {
		AddRecentViewProductRequest request = new AddRecentViewProductRequest(10001L);
		RecentViewProduct recentViewProduct = RecentViewProduct.create(1L, 10001L);
		LocalDateTime oldViewedAt = LocalDateTime.now().minusDays(1);
		ReflectionTestUtils.setField(recentViewProduct, "viewedAt", oldViewedAt);

		when(userRepository.existsById(1L)).thenReturn(true);
		when(recentViewProductRepository.findByUserIdAndProductId(1L, 10001L))
			.thenReturn(Optional.of(recentViewProduct));

		recentViewProductService.addRecentViewProduct(1L, request);

		assertThat(recentViewProduct.getViewedAt()).isAfter(oldViewedAt);
		verify(recentViewProductRepository, never()).save(any(RecentViewProduct.class));
	}

	@Test
	@DisplayName("최근 본 상품 목록을 최신순으로 조회한다")
	void getRecentViewProducts() {
		RecentViewProduct recentViewProduct = RecentViewProduct.create(1L, 10001L);
		ReflectionTestUtils.setField(recentViewProduct, "recentViewId", 10L);

		ProductSummaryResponse productSummary = new ProductSummaryResponse(
			10001L,
			"농심",
			"시드_신라면",
			"8800000000001",
			null,
			5000,
			4500,
			null,
			"ON_SALE",
			30,
			"IN_STOCK",
			"남은 재고 30개"
		);

		when(userRepository.existsById(1L)).thenReturn(true);
		when(recentViewProductRepository.findAllByUserIdOrderByViewedAtDescRecentViewIdDesc(
			eq(1L),
			any(Pageable.class)
		)).thenReturn(List.of(recentViewProduct));
		when(shoppingProductClient.getProductSummaryMap(List.of(10001L)))
			.thenReturn(Map.of(10001L, productSummary));

		RecentViewProductListResponse response =
			recentViewProductService.getRecentViewProducts(1L, 20);

		assertThat(response.count()).isEqualTo(1);
		assertThat(response.recentViews()).hasSize(1);
		assertThat(response.recentViews().get(0).recentViewId()).isEqualTo(10L);
		assertThat(response.recentViews().get(0).productId()).isEqualTo(10001L);
		assertThat(response.recentViews().get(0).product().productName()).isEqualTo("시드_신라면");
	}

	@Test
	@DisplayName("존재하지 않는 사용자의 최근 본 상품을 조회하면 예외가 발생한다")
	void throwExceptionWhenUserNotFound() {
		when(userRepository.existsById(999L)).thenReturn(false);

		assertThatThrownBy(() -> recentViewProductService.getRecentViewProducts(999L, 20))
			.isInstanceOf(BaseException.class)
			.hasMessage(USER_NOT_FOUND.getMessage());
	}

	@Test
	@DisplayName("최근 본 상품 조회 limit이 올바르지 않으면 예외가 발생한다")
	void throwExceptionWhenInvalidLimit() {
		when(userRepository.existsById(1L)).thenReturn(true);

		assertThatThrownBy(() -> recentViewProductService.getRecentViewProducts(1L, 0))
			.isInstanceOf(BaseException.class)
			.hasMessage(INVALID_INPUT_VALUE.getMessage());
	}
}