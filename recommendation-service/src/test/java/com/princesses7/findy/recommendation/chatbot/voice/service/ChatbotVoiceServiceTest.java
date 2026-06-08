package com.princesses7.findy.recommendation.chatbot.voice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotMessageResponse;
import com.princesses7.findy.recommendation.chatbot.service.ChatbotService;
import com.princesses7.findy.recommendation.chatbot.support.ChatbotResponseStatus;
import com.princesses7.findy.recommendation.chatbot.voice.dto.response.ChatbotVoiceMessageResponse;
import com.princesses7.findy.recommendation.chatbot.voice.dto.response.SttResponse;
import com.princesses7.findy.recommendation.chatbot.voice.external.OpenAiSttClient;

class ChatbotVoiceServiceTest {

	private final OpenAiSttClient openAiSttClient = mock(OpenAiSttClient.class);
	private final AudioFileValidator audioFileValidator = mock(AudioFileValidator.class);
	private final ChatbotService chatbotService = mock(ChatbotService.class);

	private final ChatbotVoiceService chatbotVoiceService = new ChatbotVoiceService(
		openAiSttClient,
		audioFileValidator,
		chatbotService
	);

	@Test
	@DisplayName("음성 파일을 텍스트로 변환한다")
	void transcribeAudioFile() {
		MockMultipartFile file = createAudioFile();

		given(openAiSttClient.transcribe(file))
			.willReturn("사리곰탕 재고 있어?");

		SttResponse response = chatbotVoiceService.transcribe(1L, file);

		assertThat(response.text()).isEqualTo("사리곰탕 재고 있어?");

		verify(audioFileValidator).validate(file);
		verify(openAiSttClient).transcribe(file);
		verifyNoInteractions(chatbotService);
	}

	@Test
	@DisplayName("음성 입력을 STT 변환 후 기존 챗봇 답변 생성 흐름에 연결한다")
	void replyByVoice() {
		MockMultipartFile file = createAudioFile();

		given(openAiSttClient.transcribe(file))
			.willReturn("사리곰탕 재고 있어?");

		ChatbotMessageResponse chatbotResponse = ChatbotMessageResponse.success(
			1L,
			"사리곰탕 재고는 5개 남아 있어요.",
			null,
			null,
			null
		);

		given(chatbotService.reply(
			eq(1L),
			eq(new ChatbotMessageRequest(
				null,
				1L,
				5,
				"사리곰탕 재고 있어?"
			))
		)).willReturn(chatbotResponse);

		ChatbotVoiceMessageResponse response = chatbotVoiceService.replyByVoice(
			1L,
			file,
			null,
			1L,
			5
		);

		assertThat(response.transcribedText()).isEqualTo("사리곰탕 재고 있어?");
		assertThat(response.chatbotResponse().answer()).contains("사리곰탕");
		assertThat(response.chatbotResponse().status()).isEqualTo(ChatbotResponseStatus.SUCCESS);

		verify(audioFileValidator).validate(file);
		verify(openAiSttClient).transcribe(file);
		verify(chatbotService).reply(
			eq(1L),
			eq(new ChatbotMessageRequest(
				null,
				1L,
				5,
				"사리곰탕 재고 있어?"
			))
		);
	}

	@Test
	@DisplayName("음성 입력으로 생성한 챗봇 답변이 fallback이어도 그대로 반환한다")
	void replyByVoiceWithFallbackResponse() {
		MockMultipartFile file = createAudioFile();

		given(openAiSttClient.transcribe(file))
			.willReturn("사리곰탕 재고 있어?");

		ChatbotMessageResponse fallbackResponse = ChatbotMessageResponse.fallback(
			1L,
			"일시적으로 챗봇 답변을 생성하지 못했어요. 잠시 후 다시 이용해 주세요.",
			null
		);

		given(chatbotService.reply(anyLong(), any(ChatbotMessageRequest.class)))
			.willReturn(fallbackResponse);

		ChatbotVoiceMessageResponse response = chatbotVoiceService.replyByVoice(
			1L,
			file,
			null,
			1L,
			5
		);

		assertThat(response.transcribedText()).isEqualTo("사리곰탕 재고 있어?");
		assertThat(response.chatbotResponse().status()).isEqualTo(ChatbotResponseStatus.FALLBACK);
		assertThat(response.chatbotResponse().answer()).contains("일시적으로");

		verify(audioFileValidator).validate(file);
		verify(openAiSttClient).transcribe(file);
		verify(chatbotService).reply(anyLong(), any(ChatbotMessageRequest.class));
	}

	private MockMultipartFile createAudioFile() {
		return new MockMultipartFile(
			"file",
			"test.mp3",
			"audio/mpeg",
			"audio-data".getBytes()
		);
	}
}