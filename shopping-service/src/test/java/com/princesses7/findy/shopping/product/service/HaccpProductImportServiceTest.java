package com.princesses7.findy.shopping.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.shopping.external.haccp.HaccpProductClient;
import com.princesses7.findy.shopping.external.haccp.HaccpProductMapper;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;
import com.princesses7.findy.shopping.external.ai.CategoryClassifierClient;
import com.princesses7.findy.shopping.global.exception.ErrorCode;
import com.princesses7.findy.shopping.inventory.service.InventoryService;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.request.HaccpProductBulkImportRequest;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductBulkImportResponse;
import com.princesses7.findy.shopping.product.dto.response.HaccpProductImportResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class HaccpProductImportServiceTest {

	@Mock
	private HaccpProductClient haccpProductClient;

	@Mock
	private HaccpProductMapper haccpProductMapper;

	@Mock
	private CategoryClassifierClient categoryClassifierClient;

	@Mock
	private ProductRepository productRepository;

	@Mock
	private InventoryService inventoryService;

	@InjectMocks
	private HaccpProductImportService haccpProductImportService;

	@Test
	@DisplayName("HACCP 상품이 기존 DB에 없으면 카테고리 분류 후 신규 상품으로 저장한다")
	void importByProductNameCreateProduct() {
		HaccpProductItemResponse haccpItem = createHaccpItem();
		ProductImportCommand enrichmentCommand = createEnrichmentCommand();
		ProductImportCommand createCommand = createCreateCommand();
		ProductCategoryClassificationResponse classification = createClassification();

		when(haccpProductClient.searchByProductName("마이쮸사과"))
			.thenReturn(List.of(haccpItem));
		when(haccpProductMapper.toEnrichmentCommand(haccpItem))
			.thenReturn(enrichmentCommand);
		when(productRepository.findByBarcodeAndDeletedAtIsNull("8801111187978"))
			.thenReturn(Optional.empty());
		when(productRepository.findAllByNormalizedProductName("마이쮸사과"))
			.thenReturn(List.of());
		when(haccpProductMapper.extractBrandName(haccpItem))
			.thenReturn("크라운제과");
		when(categoryClassifierClient.classify("마이쮸사과", "크라운제과", "캔디류"))
			.thenReturn(classification);
		when(haccpProductMapper.toCreateCommand(haccpItem, classification))
			.thenReturn(createCommand);
		when(productRepository.existsByBarcodeAndDeletedAtIsNull("8801111187978"))
			.thenReturn(false);
		when(productRepository.save(any(Product.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		HaccpProductImportResponse response =
			haccpProductImportService.importByProductName("마이쮸사과");

		assertThat(response.importedCount()).isEqualTo(1);
		assertThat(response.updatedCount()).isZero();
		assertThat(response.skippedCount()).isZero();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).importStatus()).isEqualTo("CREATED");

		verify(categoryClassifierClient).classify("마이쮸사과", "크라운제과", "캔디류");
		verify(productRepository).save(any(Product.class));
		verify(inventoryService).createDefaultInventory(any(Product.class));
	}

	@Test
	@DisplayName("HACCP 상품이 기존 DB에 있으면 OpenAI 호출 없이 누락 필드만 보강한다")
	void importByProductNameUpdateExistingProduct() {
		HaccpProductItemResponse haccpItem = createHaccpItem();
		ProductImportCommand enrichmentCommand = createEnrichmentCommand();
		Product existingProduct = Product.create(new ProductImportCommand(
			21L,
			null,
			"마이쮸사과",
			null,
			null,
			null,
			1200,
			null,
			null,
			null,
			null,
			null,
			null,
			SaleStatus.ON_SALE,
			null,
			null,
			false
		));

		when(haccpProductClient.searchByProductName("마이쮸사과"))
			.thenReturn(List.of(haccpItem));
		when(haccpProductMapper.toEnrichmentCommand(haccpItem))
			.thenReturn(enrichmentCommand);
		when(productRepository.findByBarcodeAndDeletedAtIsNull("8801111187978"))
			.thenReturn(Optional.empty());
		when(productRepository.findAllByNormalizedProductName("마이쮸사과"))
			.thenReturn(List.of(existingProduct));
		when(productRepository.existsByBarcodeAndDeletedAtIsNull("8801111187978"))
			.thenReturn(false);

		HaccpProductImportResponse response =
			haccpProductImportService.importByProductName("마이쮸사과");

		assertThat(response.importedCount()).isZero();
		assertThat(response.updatedCount()).isEqualTo(1);
		assertThat(response.skippedCount()).isZero();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).importStatus()).isEqualTo("UPDATED");
		assertThat(response.items().get(0).updatedFields()).contains("barcode", "imageUrl", "volume");

		verify(categoryClassifierClient, never()).classify(any(), any(), any());
		verify(haccpProductMapper, never()).toCreateCommand(any(), any());
		verify(productRepository, never()).save(any(Product.class));
		verify(inventoryService, never()).createDefaultInventory(any(Product.class));
	}

	@Test
	@DisplayName("HACCP 조회 결과가 없으면 상품 없음 예외를 던진다")
	void importByProductNameWithoutHaccpResult() {
		when(haccpProductClient.searchByProductName("없는상품"))
			.thenReturn(List.of());

		assertThatThrownBy(() -> haccpProductImportService.importByProductName("없는상품"))
			.isInstanceOf(ProductException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.HACCP_PRODUCT_NOT_FOUND);
	}

	private HaccpProductItemResponse createHaccpItem() {
		return new HaccpProductItemResponse(
			"19970443119140",
			null,
			"마이쮸사과",
			"백설탕, 물엿",
			"알수없음",
			"열량 35kcal",
			"8801111187978",
			"캔디류",
			"알수없음",
			"㈜크라운제과/충북 진천군",
			"㈜크라운제과/충북 진천군",
			"132g",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-1.jpg",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-2.jpg"
		);
	}

	private ProductImportCommand createEnrichmentCommand() {
		return new ProductImportCommand(
			null,
			"크라운제과",
			"마이쮸사과",
			"8801111187978",
			"HACCP",
			"19970443119140",
			0,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-1.jpg",
			"1개",
			"132g",
			null,
			"캔디류",
			SaleStatus.ON_SALE,
			null,
			"NONE",
			null
		);
	}

	private ProductImportCommand createCreateCommand() {
		return new ProductImportCommand(
			21L,
			"크라운제과",
			"마이쮸사과",
			"8801111187978",
			"HACCP",
			"19970443119140",
			0,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-1.jpg",
			"1개",
			"132g",
			null,
			"캔디류",
			SaleStatus.ON_SALE,
			new BigDecimal("0.90"),
			"OPENAI",
			false
		);
	}

	private ProductCategoryClassificationResponse createClassification() {
		return new ProductCategoryClassificationResponse(
			21L,
			"가공/냉동 식품 > 스낵/캔디 > 초콜릿/젤리",
			new BigDecimal("0.90"),
			false,
			"캔디류 상품으로 판단했습니다."
		);
	}

	@Test
	@DisplayName("HACCP bulk import는 여러 키워드의 import 결과를 합산한다")
	void bulkImportProducts() {
		HaccpProductItemResponse haccpItem = createRamenHaccpItem();
		ProductImportCommand enrichmentCommand = createRamenEnrichmentCommand();
		ProductImportCommand createCommand = createRamenCreateCommand();
		ProductCategoryClassificationResponse classification = createRamenClassification();

		when(haccpProductClient.searchByProductName("라면"))
			.thenReturn(List.of(haccpItem));
		when(haccpProductMapper.toEnrichmentCommand(haccpItem))
			.thenReturn(enrichmentCommand);
		when(productRepository.findByBarcodeAndDeletedAtIsNull("8801128508346"))
			.thenReturn(Optional.empty());
		when(productRepository.findAllByNormalizedProductName("틈새라면왕컵"))
			.thenReturn(List.of());
		when(haccpProductMapper.extractBrandName(haccpItem))
			.thenReturn("팔도");
		when(categoryClassifierClient.classify("틈새라면왕컵", "팔도", "유탕면류(용기면)"))
			.thenReturn(classification);
		when(haccpProductMapper.toCreateCommand(haccpItem, classification))
			.thenReturn(createCommand);
		when(productRepository.existsByBarcodeAndDeletedAtIsNull("8801128508346"))
			.thenReturn(false);
		when(productRepository.save(any(Product.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		HaccpProductBulkImportRequest request = new HaccpProductBulkImportRequest(
			List.of("라면"),
			10,
			true,
			true
		);

		HaccpProductBulkImportResponse response = haccpProductImportService.bulkImport(request);

		assertThat(response.keywordCount()).isEqualTo(1);
		assertThat(response.createdCount()).isEqualTo(1);
		assertThat(response.updatedCount()).isZero();
		assertThat(response.skippedCount()).isZero();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).keyword()).isEqualTo("라면");
		assertThat(response.items().get(0).productName()).isEqualTo("틈새라면왕컵");
		assertThat(response.items().get(0).barcode()).isEqualTo("8801128508346");
		assertThat(response.items().get(0).importStatus()).isEqualTo("CREATED");
	}

	private HaccpProductItemResponse createRamenHaccpItem() {
		return new HaccpProductItemResponse(
			"2012051205563",
			null,
			"틈새라면왕컵",
			"면/소맥분, 팜유, 감자전분",
			"난류,우유,대두,밀,돼지고기,닭고기,쇠고기,오징어,조개류 함유",
			"1회 제공량 1개(110g) 열량 490kcal",
			"8801128508346",
			"유탕면류(용기면)",
			"알수없음",
			"㈜팔도/본사:서울특별시 서초구 강남대로 577",
			"㈜GS리테일:경기도 용인시 처인구 포곡읍 포곡로 100",
			"110g",
			"http://www.haccp.or.kr/fresh/prdimg/2012/2012051205563/2012051205563-1.jpg",
			"http://www.haccp.or.kr/fresh/prdimg/2012/2012051205563/2012051205563-2.jpg"
		);
	}

	private ProductImportCommand createRamenEnrichmentCommand() {
		return new ProductImportCommand(
			null,
			"팔도",
			"틈새라면왕컵",
			"8801128508346",
			"HACCP",
			"2012051205563",
			0,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/2012/2012051205563/2012051205563-1.jpg",
			"1개",
			"110g",
			"난류,우유,대두,밀,돼지고기,닭고기,쇠고기,오징어,조개류 함유",
			"유탕면류(용기면)",
			SaleStatus.ON_SALE,
			null,
			"NONE",
			null
		);
	}

	private ProductImportCommand createRamenCreateCommand() {
		return new ProductImportCommand(
			17L,
			"팔도",
			"틈새라면왕컵",
			"8801128508346",
			"HACCP",
			"2012051205563",
			0,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/2012/2012051205563/2012051205563-1.jpg",
			"1개",
			"110g",
			"난류,우유,대두,밀,돼지고기,닭고기,쇠고기,오징어,조개류 함유",
			"유탕면류(용기면)",
			SaleStatus.ON_SALE,
			new BigDecimal("0.90"),
			"OPENAI",
			false
		);
	}

	private ProductCategoryClassificationResponse createRamenClassification() {
		return new ProductCategoryClassificationResponse(
			17L,
			"가공/냉동 식품 > 면/통조림 > 라면",
			new BigDecimal("0.90"),
			false,
			"유탕면류 용기면 상품으로 판단했습니다."
		);
	}
}