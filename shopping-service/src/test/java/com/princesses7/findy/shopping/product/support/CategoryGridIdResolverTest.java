package com.princesses7.findy.shopping.product.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CategoryGridIdResolverTest {

	@Test
	void resolveRandomGridId_returnsCandidateFromCategoryMapping() {
		Set<Long> resolved = new HashSet<>();

		for (int i = 0; i < 30; i++) {
			Long gridId = CategoryGridIdResolver.resolveRandomGridId(17L);
			assertThat(gridId).isBetween(2L, 9L);
			resolved.add(gridId);
		}

		assertThat(resolved.size()).isGreaterThan(1);
	}

	@Test
	void resolveRandomGridId_expandsRangeCandidatesForCategory14() {
		Long gridId = CategoryGridIdResolver.resolveRandomGridId(14L);

		assertThat(gridId).isIn(
			111L, 112L, 113L, 114L,
			140L, 141L, 142L, 143L,
			80L, 109L, 138L, 167L
		);
	}

	@Test
	void resolveRandomGridId_returnsNullForUnknownCategory() {
		assertThat(CategoryGridIdResolver.resolveRandomGridId(999L)).isNull();
		assertThat(CategoryGridIdResolver.resolveRandomGridId(null)).isNull();
	}
}
