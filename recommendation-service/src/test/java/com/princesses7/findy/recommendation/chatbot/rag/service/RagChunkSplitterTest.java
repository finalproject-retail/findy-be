package com.princesses7.findy.recommendation.chatbot.rag.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RagChunkSplitterTest {

	private final RagChunkSplitter ragChunkSplitter = new RagChunkSplitter();

	@Test
	@DisplayName("짧은 문서는 하나의 청크로 분리한다")
	void splitShortDocument() {
		List<String> chunks = ragChunkSplitter.split("Findy 이용 안내입니다.");

		assertThat(chunks).hasSize(1);
		assertThat(chunks.get(0)).isEqualTo("Findy 이용 안내입니다.");
	}

	@Test
	@DisplayName("긴 문서는 여러 청크로 분리한다")
	void splitLongDocument() {
		String content = """
			Findy 이용 안내입니다.
			
			Findy는 매장 내 상품 검색, 쇼핑리스트 생성, 재고 확인, 쿠폰 안내를 제공합니다.
			
			사용자는 챗봇을 통해 상품 위치, 행사 정보, 쿠폰 정보를 확인할 수 있습니다.
			
			챗봇 답변은 등록된 FAQ와 운영 정책 문서를 기반으로 제공될 수 있습니다.
			""".repeat(20);

		List<String> chunks = ragChunkSplitter.split(content);

		assertThat(chunks).hasSizeGreaterThan(1);
		assertThat(chunks).allMatch(chunk -> chunk.length() <= 700);
	}
}