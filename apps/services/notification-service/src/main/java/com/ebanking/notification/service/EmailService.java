package com.ebanking.notification.service;

import com.ebanking.notification.config.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Service for sending email notifications. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final EmailConfig emailConfig;

  /** Send a simple text email */
  public void sendSimpleEmail(String to, String subject, String text) throws RuntimeException {
    if (!emailConfig.isEnabled()) {
      log.warn("Email service is disabled. Skipping email to: {}", to);
      throw new RuntimeException("Email service is disabled");
    }

    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(emailConfig.getFromAddress());
      message.setTo(to);
      message.setSubject(subject);
      message.setText(text);

      mailSender.send(message);
      log.info("Simple email sent successfully to: {}", to);

    } catch (Exception e) {
      log.error("Failed to send simple email to: {}", to, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  /** Send an HTML email */
  public void sendHtmlEmail(String to, String subject, String htmlContent) throws RuntimeException {
    if (!emailConfig.isEnabled()) {
      log.warn("Email service is disabled. Skipping email to: {}", to);
      throw new RuntimeException("Email service is disabled");
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true); // true = isHtml

      mailSender.send(message);
      log.info("HTML email sent successfully to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send HTML email to: {}", to, e);
      throw new RuntimeException("Failed to send HTML email", e);
    } catch (Exception e) {
      log.error("Unexpected error sending email to: {}", to, e);
      throw new RuntimeException("Failed to send email", e);
    }
  }

  /** Check if email service is enabled */
  public boolean isEnabled() {
    return emailConfig.isEnabled();
  }
}
