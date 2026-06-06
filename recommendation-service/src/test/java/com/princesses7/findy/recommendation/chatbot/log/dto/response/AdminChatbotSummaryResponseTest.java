package com.princesses7.findy.recommendation.chatbot.log.dto.response;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.recommendation.chatbot.log.repository.projection.ChatbotLogSummaryProjection;

class AdminChatbotSummaryResponseTest {

	@Test
	@DisplayName("챗봇 실패율을 계산한다")
	void calculateFailureRate() {
		ChatbotLogSummaryProjection summary = new ChatbotLogSummaryProjection() {

			@Override
			public Long getTotalCount() {
				return 10L;
			}

			@Override
			public Long getSuccessCount() {
				return 8L;
			}

			@Override
			public Long getFailureCount() {
				return 2L;
			}

			@Override
			public Double getAverageDurationMs() {
				return 120.0;
			}
		};

		AdminChatbotSummaryResponse response = AdminChatbotSummaryResponse.of(
			summary,
			List.of()
		);

		assertThat(response.totalCount()).isEqualTo(10L);
		assertThat(response.successCount()).isEqualTo(8L);
		assertThat(response.failureCount()).isEqualTo(2L);
		assertThat(response.failureRate()).isEqualByComparingTo("20.00");
		assertThat(response.averageDurationMs()).isEqualTo(120.0);
	}
}