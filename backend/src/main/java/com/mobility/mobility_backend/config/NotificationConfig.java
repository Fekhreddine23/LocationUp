package com.mobility.mobility_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class NotificationConfig {

	@Value("${spring.mail.host:localhost}")
	private String host;

	@Value("${spring.mail.port:1025}")
	private int port;

	@Value("${spring.mail.username:}")
	private String username;

	@Value("${spring.mail.password:}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth:false}")
	private boolean smtpAuth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
	private boolean startTls;

	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setPort(port);
		sender.setUsername(username.isBlank() ? null : username);
		sender.setPassword(password.isBlank() ? null : password);

		Properties props = sender.getJavaMailProperties();
		props.put("mail.smtp.auth", smtpAuth);
		props.put("mail.smtp.starttls.enable", startTls);
		return sender;
	}
}
