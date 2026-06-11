package com.princesses7.findy.user.email.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindyEmailSender implements EmailSender {

	private final ObjectProvider<JavaMailSender> javaMailSenderProvider;

	@Value("${findy.mail.enabled:false}")
	private boolean mailEnabled;

	@Value("${findy.mail.from:no-reply@findy.local}")
	private String from;

	@Override
	public void send(String to, String subject, String text) {
		if (!mailEnabled) {
			log.info("[DEV MAIL] to={}, subject={}, text={}", to, subject, text);
			return;
		}

		JavaMailSender javaMailSender = javaMailSenderProvider.getIfAvailable();
		if (javaMailSender == null) {
			throw new BaseException(ErrorCode.EMAIL_SEND_FAILED, "메일 발송 설정이 필요합니다.");
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			javaMailSender.send(message);
		} catch (MailException exception) {
			throw new BaseException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}
}
