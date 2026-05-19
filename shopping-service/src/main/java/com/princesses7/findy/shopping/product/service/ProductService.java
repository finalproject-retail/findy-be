package com.princesses7.findy.shopping.product.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.external.mfds.MfdsBarcodeClient;
import com.princesses7.findy.shopping.external.mfds.MfdsProductMapper;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeItemResponse;
import com.princesses7.findy.shopping.external.mfds.dto.response.MfdsBarcodeResponse;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.dto.response.ProductDetailResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductPageResponse;
import com.princesses7.findy.shopping.product.dto.response.ProductResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.exception.ProductException;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private static final int MAX_PAGE_SIZE = 100;

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
		"productId",
		"productName",
		"originalPrice",
		"salePrice",
		"discountRate",
		"createdAt"
	);

	private final MfdsBarcodeClient mfdsBarcodeClient;
	private final MfdsProductMapper mfdsProductMapper;
	private final ProductRepository productRepository;

	public ProductPageResponse getProducts(
		Long categoryId,
		int page,
		int size,
		String sortBy,
		String direction
	) {
		validatePageRequest(page, size);

		Sort sort = createSort(sortBy, direction);
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> products = categoryId == null
			? productRepository.findByIsDeletedFalse(pageable)
			: productRepository.findByCategoryIdAndIsDeletedFalse(categoryId, pageable);

		Page<ProductResponse> responsePage = products.map(ProductResponse::from);

		return ProductPageResponse.from(responsePage);
	}

	public ProductDetailResponse getProductDetail(Long productId) {
		Product product = productRepository.findByProductIdAndIsDeletedFalse(productId)
			.orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

		return ProductDetailResponse.from(product);
	}

	private void validatePageRequest(int page, int size) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new ProductException(INVALID_INPUT_VALUE);
		}
	}

	private Sort createSort(String sortBy, String direction) {
		// TODO: 인기순 정렬은 Redis 랭킹 데이터 연동 시 별도 구현
		String sortProperty = sortBy == null || sortBy.isBlank()
			? "createdAt"
			: sortBy;

		if (!ALLOWED_SORT_PROPERTIES.contains(sortProperty)) {
			throw new ProductException(INVALID_SORT_TYPE);
		}

		Sort.Direction sortDirection = parseDirection(direction);

		return Sort.by(sortDirection, sortProperty);
	}

	private Sort.Direction parseDirection(String direction) {
		if (direction == null || direction.isBlank() || "asc".equalsIgnoreCase(direction)) {
			return Sort.Direction.ASC;
		}

		if ("desc".equalsIgnoreCase(direction)) {
			return Sort.Direction.DESC;
		}

		throw new ProductException(INVALID_SORT_TYPE);
	}

	@Transactional
	public Long importByBarcode(String barcode) {
		if (productRepository.existsByBarcodeAndIsDeletedFalse(barcode)) {
			throw new ProductException(DUPLICATE_BARCODE);
		}

		MfdsBarcodeResponse response = mfdsBarcodeClient.searchByBarcode(barcode);
		List<MfdsBarcodeItemResponse> items = response.getItems();

		if (items.isEmpty()) {
			throw new ProductException(BARCODE_PRODUCT_NOT_FOUND);
		}

		ProductImportCommand command = mfdsProductMapper.toCommand(items.get(0));
		Product product = Product.create(command);

		return productRepository.save(product).getProductId();
	}
}