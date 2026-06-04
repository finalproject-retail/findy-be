package com.princesses7.findy.shopping.external.ai;

import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;

public interface CategoryClassifierClient {

	ProductCategoryClassificationResponse classify(
		String productName,
		String brandName,
		String externalCategory
	);
}
