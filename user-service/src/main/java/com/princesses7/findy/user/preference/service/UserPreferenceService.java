package com.princesses7.findy.user.preference.service;

import static com.princesses7.findy.user.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.preference.dto.request.SavePreferenceRequest;
import com.princesses7.findy.user.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.user.preference.entity.UserPreferredCategory;
import com.princesses7.findy.user.preference.entity.UserShoppingStyle;
import com.princesses7.findy.user.preference.repository.UserPreferredCategoryRepository;
import com.princesses7.findy.user.preference.repository.UserShoppingStyleRepository;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

	private final UserRepository userRepository;
	private final UserPreferredCategoryRepository userPreferredCategoryRepository;
	private final UserShoppingStyleRepository userShoppingStyleRepository;

	@Transactional
	public void savePreferences(Long userId, SavePreferenceRequest request) {
		validateRequest(request);

		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(USER_NOT_FOUND));

		userPreferredCategoryRepository.deleteAllByUserId(userId);
		userShoppingStyleRepository.deleteAllByUserId(userId);

		userPreferredCategoryRepository.saveAll(
			request.categoryIds().stream()
				.distinct()
				.map(categoryId -> UserPreferredCategory.create(userId, categoryId))
				.toList()
		);

		userShoppingStyleRepository.saveAll(
			request.shoppingStyleIds().stream()
				.distinct()
				.map(styleId -> UserShoppingStyle.create(userId, styleId))
				.toList()
		);

		user.completeOnboarding();
	}

	@Transactional(readOnly = true)
	public UserPreferenceResponse getMyPreferences(Long userId) {
		List<Long> categoryIds = userPreferredCategoryRepository.findAllByUserId(userId)
			.stream()
			.map(UserPreferredCategory::getCategoryId)
			.toList();

		List<Long> shoppingStyleIds = userShoppingStyleRepository.findAllByUserId(userId)
			.stream()
			.map(UserShoppingStyle::getShoppingStyleId)
			.toList();

		return new UserPreferenceResponse(userId, categoryIds, shoppingStyleIds);
	}

	private void validateRequest(SavePreferenceRequest request) {
		if (request.categoryIds() == null || request.categoryIds().isEmpty()) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}

		if (request.shoppingStyleIds() == null || request.shoppingStyleIds().isEmpty()) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}
	}
}