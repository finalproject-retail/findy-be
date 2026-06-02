package com.princesses7.findy.shopping.analytics.support;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.category.entity.Category;
import com.princesses7.findy.shopping.category.repository.CategoryRepository;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductAnalyticsMetadataResolver {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public ProductAnalyticsMetadata resolve(Long productId) {
		if (productId == null) {
			return new ProductAnalyticsMetadata(null, null, null);
		}

		return productRepository.findById(productId)
			.map(this::toMetadata)
			.orElseGet(() -> new ProductAnalyticsMetadata(productId, null, null));
	}

	private ProductAnalyticsMetadata toMetadata(Product product) {
		Long categoryId = product.getCategoryId();
		Long gridId = categoryRepository.findById(categoryId)
			.map(Category::getGridId)
			.orElse(null);

		return new ProductAnalyticsMetadata(product.getProductId(), categoryId, gridId);
	}
}
