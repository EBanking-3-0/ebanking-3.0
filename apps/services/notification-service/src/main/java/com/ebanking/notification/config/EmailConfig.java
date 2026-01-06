package com.ebanking.notification.config;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Configuration for email sending using JavaMailSender. */
@Slf4j
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class EmailConfig {

  /**
   * Configure JavaMailSender bean for sending emails.
   *
   * @param mailProperties Mail properties from application.yml
   * @return Configured JavaMailSender
   */
  @Bean
  public JavaMailSender javaMailSender(MailProperties mailProperties) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    // Basic configuration
    mailSender.setHost(mailProperties.getHost());
    mailSender.setPort(mailProperties.getPort());
    mailSender.setUsername(mailProperties.getUsername());
    mailSender.setPassword(mailProperties.getPassword());

    // Additional properties
    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", mailProperties.getProperties().get("mail.smtp.auth"));
    props.put(
        "mail.smtp.starttls.enable",
        mailProperties.getProperties().get("mail.smtp.starttls.enable"));
    props.put("mail.debug", mailProperties.getProperties().getOrDefault("mail.debug", "false"));

    log.info("Email configuration initialized for host: {}", mailProperties.getHost());

    return mailSender;
  }
}
