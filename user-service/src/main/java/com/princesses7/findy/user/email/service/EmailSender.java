package com.princesses7.findy.user.email.service;

public interface EmailSender {

	void send(String to, String subject, String text);
}
