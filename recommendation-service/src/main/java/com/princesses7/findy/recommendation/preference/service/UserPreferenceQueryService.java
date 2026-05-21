package com.princesses7.findy.recommendation.preference.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.entity.ShoppingStyleSnapshot;
import com.princesses7.findy.recommendation.preference.entity.UserPreferredCategorySnapshot;
import com.princesses7.findy.recommendation.preference.entity.UserShoppingStyleSnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.repository.ShoppingStyleSnapshotRepository;
import com.princesses7.findy.recommendation.preference.repository.UserPreferredCategorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.repository.UserShoppingStyleSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferenceQueryService {

	private final UserPreferredCategorySnapshotRepository userPreferredCategoryRepository;
	private final UserShoppingStyleSnapshotRepository userShoppingStyleRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final ShoppingStyleSnapshotRepository shoppingStyleRepository;

	@Transactional(readOnly = true)
	public UserPreferenceResponse getUserPreference(Long userId) {
		List<Long> categoryIds = userPreferredCategoryRepository.findByUserId(userId)
			.stream()
			.map(UserPreferredCategorySnapshot::getCategoryId)
			.toList();

		List<Long> shoppingStyleIds = userShoppingStyleRepository.findByUserId(userId)
			.stream()
			.map(UserShoppingStyleSnapshot::getShoppingStyleId)
			.toList();

		List<String> categoryNames = findCategoryNames(categoryIds);
		List<String> styleNames = findShoppingStyleNames(shoppingStyleIds);

		return new UserPreferenceResponse(
			userId,
			categoryNames,
			styleNames,
			createPreferenceText(categoryNames, styleNames)
		);
	}

	public boolean hasPreference(UserPreferenceResponse response) {
		return !response.preferredCategories().isEmpty()
			|| !response.shoppingStyles().isEmpty();
	}

	private List<String> findCategoryNames(List<Long> categoryIds) {
		if (categoryIds.isEmpty()) {
			return List.of();
		}

		Map<Long, String> categoryNameMap = categoryRepository.findByCategoryIdIn(categoryIds)
			.stream()
			.filter(CategorySnapshot::isActive)
			.collect(Collectors.toMap(
				CategorySnapshot::getCategoryId,
				CategorySnapshot::getCategoryName
			));

		return categoryIds.stream()
			.map(categoryNameMap::get)
			.filter(name -> name != null && !name.isBlank())
			.toList();
	}

	private List<String> findShoppingStyleNames(List<Long> shoppingStyleIds) {
		if (shoppingStyleIds.isEmpty()) {
			return List.of();
		}

		Map<Long, String> styleNameMap = shoppingStyleRepository.findByShoppingStyleIdIn(shoppingStyleIds)
			.stream()
			.filter(ShoppingStyleSnapshot::isActive)
			.collect(Collectors.toMap(
				ShoppingStyleSnapshot::getShoppingStyleId,
				ShoppingStyleSnapshot::getStyleName
			));

		return shoppingStyleIds.stream()
			.map(styleNameMap::get)
			.filter(name -> name != null && !name.isBlank())
			.toList();
	}

	private String createPreferenceText(
		List<String> categoryNames,
		List<String> styleNames
	) {
		return """
			사용자의 오프라인 마트 장보기 선호 정보입니다.
			선호 카테고리: %s
			쇼핑 스타일: %s
			이 사용자가 관심을 가질 만한 상품을 추천합니다.
			""".formatted(
			String.join(", ", categoryNames),
			String.join(", ", styleNames)
		);
	}
}