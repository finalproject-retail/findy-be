package com.princesses7.findy.recommendation.external.rerank;

import java.util.List;
import java.util.Map;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankItem;

public interface RelatedProductRerankClient {

	List<RelatedProductRerankItem> rerank(
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		List<ProductSnapshot> candidates,
		Map<Long, String> categoryNameMap,
		int size
	);
}
