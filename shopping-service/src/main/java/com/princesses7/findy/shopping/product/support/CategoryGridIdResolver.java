package com.princesses7.findy.shopping.product.support;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Leaf category(1..46)별 후보 grid_id 중 하나를 랜덤으로 선택한다.
 * AI 카테고리 분류 후 상품 생성 시 products.grid_id가 null로 남지 않도록 한다.
 */
public final class CategoryGridIdResolver {

	private static final Map<Long, List<Long>> GRID_IDS_BY_CATEGORY_ID = Map.ofEntries(
		Map.entry(1L, List.of(519L, 520L, 521L, 522L)),
		Map.entry(2L, List.of(406L, 435L, 464L, 493L)),
		Map.entry(3L, List.of(1L, 30L, 59L, 88L, 117L, 146L, 175L, 204L)),
		Map.entry(4L, List.of(26L, 27L, 28L)),
		Map.entry(5L, List.of(29L, 58L, 87L)),
		Map.entry(6L, List.of(116L, 145L)),
		Map.entry(7L, List.of(174L, 203L, 232L, 261L)),
		Map.entry(8L, List.of(290L, 319L)),
		Map.entry(9L, List.of(348L, 377L)),
		Map.entry(10L, List.of(79L, 108L, 137L, 166L, 77L, 106L)),
		Map.entry(11L, List.of(135L, 164L)),
		Map.entry(12L, List.of(224L, 253L, 282L, 311L)),
		Map.entry(13L, List.of(369L, 398L, 427L, 456L)),
		Map.entry(14L, List.of(111L, 112L, 113L, 114L, 140L, 141L, 142L, 143L, 80L, 109L, 138L, 167L)),
		Map.entry(15L, List.of(256L, 257L, 285L, 286L, 287L, 288L, 225L, 254L, 283L, 312L)),
		Map.entry(16L, List.of(430L, 431L, 432L, 433L, 401L, 402L, 403L, 404L, 370L, 399L, 428L, 457L)),
		Map.entry(17L, List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)),
		Map.entry(18L, List.of(71L, 100L, 129L, 158L)),
		Map.entry(19L, List.of(73L, 102L, 131L, 160L)),
		Map.entry(20L, List.of(61L, 90L, 119L, 148L, 62L, 91L, 120L, 149L, 64L, 93L, 122L, 151L)),
		Map.entry(21L, List.of(65L, 94L, 123L, 152L, 67L, 96L, 125L, 154L)),
		Map.entry(22L, List.of(68L, 97L, 126L, 155L, 70L, 99L, 128L, 157L)),
		Map.entry(23L, List.of(10L, 11L, 12L, 13L, 14L)),
		Map.entry(24L, List.of(15L)),
		Map.entry(25L, List.of(16L, 17L)),
		Map.entry(26L, List.of(103L, 161L)),
		Map.entry(27L, List.of(76L, 105L, 134L, 163L)),
		Map.entry(28L, List.of(74L, 132L)),
		Map.entry(29L, List.of(367L, 396L)),
		Map.entry(30L, List.of(222L, 251L, 280L, 309L)),
		Map.entry(31L, List.of(425L, 454L)),
		Map.entry(32L, List.of(514L, 515L)),
		Map.entry(33L, List.of(511L, 512L, 513L)),
		Map.entry(34L, List.of(516L, 517L, 518L)),
		Map.entry(35L, List.of(355L, 384L)),
		Map.entry(36L, List.of(357L, 386L, 415L, 444L)),
		Map.entry(37L, List.of(413L, 442L)),
		Map.entry(38L, List.of(503L, 504L, 505L, 506L, 507L, 508L, 509L, 510L)),
		Map.entry(39L, List.of(358L, 387L, 416L, 445L, 360L, 389L, 418L, 447L)),
		Map.entry(40L, List.of(361L, 390L, 419L, 448L, 363L, 392L, 421L, 450L, 364L, 393L, 422L, 451L, 366L, 395L, 424L, 453L)),
		Map.entry(41L, List.of(351L, 352L, 354L, 380L, 381L, 383L, 409L, 410L, 412L, 438L, 439L, 441L)),
		Map.entry(42L, List.of(206L, 235L, 264L, 293L, 207L, 236L, 265L, 294L)),
		Map.entry(43L, List.of(209L, 210L, 212L, 213L, 238L, 239L, 241L, 242L, 267L, 268L, 270L, 271L, 296L, 297L, 299L, 300L)),
		Map.entry(44L, List.of(215L, 216L, 218L, 244L, 245L, 247L, 276L, 302L, 303L, 305L)),
		Map.entry(45L, List.of(219L, 248L, 277L, 306L, 221L, 250L, 279L, 308L))
	);

	private CategoryGridIdResolver() {
	}

	public static Long resolveRandomGridId(Long categoryId) {
		if (categoryId == null) {
			return null;
		}

		List<Long> candidates = GRID_IDS_BY_CATEGORY_ID.get(categoryId);
		if (candidates == null || candidates.isEmpty()) {
			return null;
		}

		int index = ThreadLocalRandom.current().nextInt(candidates.size());
		return candidates.get(index);
	}
}
