package com.ebanking.notification.config;

import java.util.Properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Email configuration for sending notifications. */
@Configuration
@ConfigurationProperties(prefix = "notification.email")
@Data
public class EmailConfig {

  private String host;
  private int port;
  private String username;
  private String password;
  private String fromAddress;
  private String fromName;
  private boolean enabled;

  // SMTP properties
  private boolean smtpAuth;
  private boolean smtpStartTlsEnable;
  private boolean smtpStartTlsRequired;
  private String smtpConnectionTimeout;
  private String smtpTimeout;
  private String smtpWriteTimeout;
  private String mailDebug;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", smtpAuth);
    props.put("mail.smtp.starttls.enable", smtpStartTlsEnable);
    props.put("mail.smtp.starttls.required", smtpStartTlsRequired);
    props.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
    props.put("mail.smtp.timeout", smtpTimeout);
    props.put("mail.smtp.writetimeout", smtpWriteTimeout);
    props.put("mail.debug", mailDebug);

    return mailSender;
  }
}
