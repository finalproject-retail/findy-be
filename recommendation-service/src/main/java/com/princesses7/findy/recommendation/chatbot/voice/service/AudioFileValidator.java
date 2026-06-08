package com.princesses7.findy.recommendation.chatbot.voice.service;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;

@Component
public class AudioFileValidator {

	private static final long MAX_AUDIO_FILE_SIZE = 10 * 1024 * 1024;

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"audio/mpeg",
		"audio/mp3",
		"audio/mp4",
		"audio/m4a",
		"audio/wav",
		"audio/x-wav",
		"audio/webm",
		"audio/ogg"
	);

	public void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BaseException(ErrorCode.CHATBOT_AUDIO_FILE_REQUIRED);
		}

		if (file.getSize() > MAX_AUDIO_FILE_SIZE) {
			throw new BaseException(ErrorCode.CHATBOT_AUDIO_FILE_TOO_LARGE);
		}

		String contentType = file.getContentType();

		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new BaseException(ErrorCode.CHATBOT_AUDIO_FILE_UNSUPPORTED);
		}
	}
}