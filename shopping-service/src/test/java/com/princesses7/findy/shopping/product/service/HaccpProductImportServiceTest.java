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
import com.princesses7.findy.shopping.external.openai.OpenAiCategoryClassifierClient;
import com.princesses7.findy.shopping.global.exception.ErrorCode;
import com.princesses7.findy.shopping.inventory.service.InventoryService;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
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
	private OpenAiCategoryClassifierClient openAiCategoryClassifierClient;

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
		when(productRepository.findByBarcodeAndIsDeletedFalse("8801111187978"))
			.thenReturn(Optional.empty());
		when(productRepository.findAllByNormalizedProductName("마이쮸사과"))
			.thenReturn(List.of());
		when(haccpProductMapper.extractBrandName(haccpItem))
			.thenReturn("크라운제과");
		when(openAiCategoryClassifierClient.classify("마이쮸사과", "크라운제과", "캔디류"))
			.thenReturn(classification);
		when(haccpProductMapper.toCreateCommand(haccpItem, classification))
			.thenReturn(createCommand);
		when(productRepository.existsByBarcodeAndIsDeletedFalse("8801111187978"))
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

		verify(openAiCategoryClassifierClient).classify("마이쮸사과", "크라운제과", "캔디류");
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
			1200,
			BigDecimal.ZERO,
			null,
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
		when(productRepository.findByBarcodeAndIsDeletedFalse("8801111187978"))
			.thenReturn(Optional.empty());
		when(productRepository.findAllByNormalizedProductName("마이쮸사과"))
			.thenReturn(List.of(existingProduct));
		when(productRepository.existsByBarcodeAndIsDeletedFalse("8801111187978"))
			.thenReturn(false);

		HaccpProductImportResponse response =
			haccpProductImportService.importByProductName("마이쮸사과");

		assertThat(response.importedCount()).isZero();
		assertThat(response.updatedCount()).isEqualTo(1);
		assertThat(response.skippedCount()).isZero();
		assertThat(response.items()).hasSize(1);
		assertThat(response.items().get(0).importStatus()).isEqualTo("UPDATED");
		assertThat(response.items().get(0).updatedFields()).contains("barcode", "imageUrl", "volume");

		verify(openAiCategoryClassifierClient, never()).classify(any(), any(), any());
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
			0,
			BigDecimal.ZERO,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-1.jpg",
			null,
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
			0,
			BigDecimal.ZERO,
			"HACCP 제품이미지 및 포장지표기정보 연동 상품",
			"http://www.haccp.or.kr/fresh/prdimg/1997/19970443119140/19970443119140-1.jpg",
			null,
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
}