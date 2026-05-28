package com.princesses7.findy.shopping.product.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProductCategoryCatalog {

	private ProductCategoryCatalog() {
	}

	public static final List<CategoryCandidate> CATEGORIES = List.of(
		new CategoryCandidate(1L, "신선 식품 > 농산 > 과일"),
		new CategoryCandidate(2L, "신선 식품 > 농산 > 채소/샐러드"),
		new CategoryCandidate(3L, "신선 식품 > 농산 > 견과류"),

		new CategoryCandidate(4L, "신선 식품 > 축산 > 소고기"),
		new CategoryCandidate(5L, "신선 식품 > 축산 > 돼지고기"),
		new CategoryCandidate(6L, "신선 식품 > 축산 > 닭고기"),

		new CategoryCandidate(7L, "신선 식품 > 수산 > 회/초밥"),
		new CategoryCandidate(8L, "신선 식품 > 수산 > 수산물"),
		new CategoryCandidate(9L, "신선 식품 > 수산 > 건어물"),

		new CategoryCandidate(10L, "신선 식품 > 유제품/냉장 > 우유/요거트"),
		new CategoryCandidate(11L, "신선 식품 > 유제품/냉장 > 치즈/버터"),
		new CategoryCandidate(12L, "신선 식품 > 유제품/냉장 > 햄/소시지"),
		new CategoryCandidate(13L, "신선 식품 > 유제품/냉장 > 밀키트"),

		new CategoryCandidate(14L, "가공/냉동 식품 > 냉동 > 만두/피자"),
		new CategoryCandidate(15L, "가공/냉동 식품 > 냉동 > 간편식"),
		new CategoryCandidate(16L, "가공/냉동 식품 > 냉동 > 냉동 과일/디저트"),

		new CategoryCandidate(17L, "가공/냉동 식품 > 면/통조림 > 라면"),
		new CategoryCandidate(18L, "가공/냉동 식품 > 면/통조림 > 즉석밥"),
		new CategoryCandidate(19L, "가공/냉동 식품 > 면/통조림 > 통조림"),

		new CategoryCandidate(20L, "가공/냉동 식품 > 스낵/캔디 > 과자"),
		new CategoryCandidate(21L, "가공/냉동 식품 > 스낵/캔디 > 초콜릿/젤리"),
		new CategoryCandidate(22L, "가공/냉동 식품 > 스낵/캔디 > 시리얼"),

		new CategoryCandidate(23L, "베이커리/델리 > 베이커리 > 빵/베이글"),
		new CategoryCandidate(24L, "베이커리/델리 > 베이커리 > 케이크"),
		new CategoryCandidate(25L, "베이커리/델리 > 베이커리 > 쿠키"),

		new CategoryCandidate(26L, "베이커리/델리 > 델리 > 치킨"),
		new CategoryCandidate(27L, "베이커리/델리 > 델리 > 꼬치류"),
		new CategoryCandidate(28L, "베이커리/델리 > 델리 > 일품요리"),

		new CategoryCandidate(29L, "음료/주류 > 음료 > 생수/탄산수"),
		new CategoryCandidate(30L, "음료/주류 > 음료 > 탄산음료"),
		new CategoryCandidate(31L, "음료/주류 > 음료 > 커피/차"),

		new CategoryCandidate(32L, "음료/주류 > 주류 > 와인/양주"),
		new CategoryCandidate(33L, "음료/주류 > 주류 > 맥주"),
		new CategoryCandidate(34L, "음료/주류 > 주류 > 전통주"),

		new CategoryCandidate(35L, "라이프 스타일 > 주방/생활 > 세제/섬유유연제"),
		new CategoryCandidate(36L, "라이프 스타일 > 주방/생활 > 일회용품"),
		new CategoryCandidate(37L, "라이프 스타일 > 주방/생활 > 주방용품"),

		new CategoryCandidate(38L, "라이프 스타일 > 가전/IT > 대형 가전"),
		new CategoryCandidate(39L, "라이프 스타일 > 가전/IT > 디지털 기기"),
		new CategoryCandidate(40L, "라이프 스타일 > 가전/IT > 소형 전자제품"),

		new CategoryCandidate(41L, "라이프 스타일 > 의류/잡화 > 의류"),
		new CategoryCandidate(42L, "라이프 스타일 > 의류/잡화 > 디지털 기기"),
		new CategoryCandidate(43L, "라이프 스타일 > 의류/잡화 > 신발/가방"),

		new CategoryCandidate(44L, "라이프 스타일 > 홈케어/캠핑 > 가구/침구"),
		new CategoryCandidate(45L, "라이프 스타일 > 홈케어/캠핑 > 캠핑/아웃도어 용품"),
		new CategoryCandidate(46L, "라이프 스타일 > 홈케어/캠핑 > 차량 용품")
	);

	private static final Map<Long, String> CATEGORY_PATH_BY_ID = CATEGORIES.stream()
		.collect(Collectors.toUnmodifiableMap(
			CategoryCandidate::categoryId,
			CategoryCandidate::path
		));

	public static boolean exists(Long categoryId) {
		return categoryId != null && CATEGORY_PATH_BY_ID.containsKey(categoryId);
	}

	public static String pathOf(Long categoryId) {
		return CATEGORY_PATH_BY_ID.get(categoryId);
	}

	public static List<Long> categoryIds() {
		return CATEGORIES.stream()
			.map(CategoryCandidate::categoryId)
			.toList();
	}

	public static String createCandidateText() {
		StringBuilder builder = new StringBuilder();

		for (CategoryCandidate category : CATEGORIES) {
			builder.append(category.categoryId())
				.append(": ")
				.append(category.path())
				.append("\n");
		}

		return builder.toString();
	}

	public record CategoryCandidate(
		Long categoryId,
		String path
	) {
	}
}