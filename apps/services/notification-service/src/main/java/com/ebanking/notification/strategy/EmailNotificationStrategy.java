package com.ebanking.notification.strategy;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.exception.NotificationSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/** Strategy for sending email notifications. Uses Spring's JavaMailSender to send HTML emails. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.email", name = "enabled", havingValue = "true")
public class EmailNotificationStrategy implements NotificationStrategy {

  private final JavaMailSender mailSender;

  @Value("${notification.email.from-address}")
  private String fromAddress;

  @Value("${notification.email.from-name}")
  private String fromName;

  @Override
  public void send(Notification notification, String recipient, String subject, String content)
      throws NotificationSendException {

    try {
      log.info("Sending email notification to: {} for type: {}", recipient, notification.getType());

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromAddress, fromName);
      helper.setTo(recipient);
      helper.setSubject(subject);
      helper.setText(content, true); // true = HTML content

      mailSender.send(message);

      log.info(
          "Email notification sent successfully to: {} (ID: {})", recipient, notification.getId());

    } catch (MessagingException e) {
      log.error("Failed to send email notification to: {}", recipient, e);
      throw new NotificationSendException("Failed to send email: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error sending email notification to: {}", recipient, e);
      throw new NotificationSendException("Unexpected error sending email: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean supports(String recipient) {
    // Basic email validation
    return recipient != null
        && recipient.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");
  }

  @Override
  public String getChannelName() {
    return "EMAIL";
  }
}
