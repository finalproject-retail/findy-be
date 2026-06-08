package com.princesses7.findy.recommendation.chatbot.voice.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.princesses7.findy.recommendation.global.exception.BaseException;

class AudioFileValidatorTest {

	private final AudioFileValidator audioFileValidator = new AudioFileValidator();

	@Test
	@DisplayName("지원하는 음성 파일이면 검증을 통과한다")
	void validateSupportedAudioFile() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"test.mp3",
			"audio/mpeg",
			"audio-data".getBytes()
		);

		assertThatCode(() -> audioFileValidator.validate(file))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("음성 파일이 없으면 예외가 발생한다")
	void validateEmptyAudioFile() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"empty.mp3",
			"audio/mpeg",
			new byte[0]
		);

		assertThatThrownBy(() -> audioFileValidator.validate(file))
			.isInstanceOf(BaseException.class);
	}

	@Test
	@DisplayName("지원하지 않는 파일 형식이면 예외가 발생한다")
	void validateUnsupportedContentType() {
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"test.txt",
			"text/plain",
			"not-audio".getBytes()
		);

		assertThatThrownBy(() -> audioFileValidator.validate(file))
			.isInstanceOf(BaseException.class);
	}

	@Test
	@DisplayName("음성 파일 크기가 10MB를 초과하면 예외가 발생한다")
	void validateTooLargeAudioFile() {
		byte[] largeContent = new byte[10 * 1024 * 1024 + 1];

		MockMultipartFile file = new MockMultipartFile(
			"file",
			"large.mp3",
			"audio/mpeg",
			largeContent
		);

		assertThatThrownBy(() -> audioFileValidator.validate(file))
			.isInstanceOf(BaseException.class);
	}
}