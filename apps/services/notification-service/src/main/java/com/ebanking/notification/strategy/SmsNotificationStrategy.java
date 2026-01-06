package com.ebanking.notification.strategy;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.exception.NotificationSendException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Strategy for sending SMS notifications using Twilio. Requires Twilio account credentials to be
 * configured.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "notification.twilio", name = "enabled", havingValue = "true")
public class SmsNotificationStrategy implements NotificationStrategy {

  @Value("${notification.twilio.account-sid}")
  private String accountSid;

  @Value("${notification.twilio.auth-token}")
  private String authToken;

  @Value("${notification.twilio.phone-number}")
  private String fromPhoneNumber;

  @PostConstruct
  public void init() {
    if (accountSid != null && authToken != null) {
      Twilio.init(accountSid, authToken);
      log.info("Twilio SMS notification strategy initialized");
    } else {
      log.warn("Twilio credentials not configured, SMS notifications will not be available");
    }
  }

  @Override
  public void send(Notification notification, String recipient, String subject, String content)
      throws NotificationSendException {

    try {
      log.info("Sending SMS notification to: {} for type: {}", recipient, notification.getType());

      // Combine subject and content for SMS
      String smsBody = subject + "\n\n" + content;

      // Truncate if too long (SMS limit is 1600 characters for concatenated messages)
      if (smsBody.length() > 1600) {
        smsBody = smsBody.substring(0, 1597) + "...";
      }

      Message message =
          Message.creator(
                  new PhoneNumber(recipient), // To
                  new PhoneNumber(fromPhoneNumber), // From
                  smsBody)
              .create();

      log.info(
          "SMS notification sent successfully to: {} (SID: {}, Status: {})",
          recipient,
          message.getSid(),
          message.getStatus());

    } catch (Exception e) {
      log.error("Failed to send SMS notification to: {}", recipient, e);
      throw new NotificationSendException("Failed to send SMS: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean supports(String recipient) {
    // Basic phone number validation (international format)
    return recipient != null && recipient.matches("^\\+?[1-9]\\d{1,14}$");
  }

  @Override
  public String getChannelName() {
    return "SMS";
  }
}
