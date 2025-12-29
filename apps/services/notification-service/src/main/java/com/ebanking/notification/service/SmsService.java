package com.ebanking.notification.service;

import com.ebanking.notification.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service for sending SMS notifications via Twilio. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

  private final TwilioConfig twilioConfig;

  /** Send an SMS message */
  public void sendSms(String to, String messageBody) {
    if (!twilioConfig.isEnabled()) {
      log.warn("SMS service is disabled. Skipping SMS to: {}", to);
      return;
    }

    if (twilioConfig.getPhoneNumber() == null) {
      log.error("Twilio phone number not configured. Cannot send SMS.");
      throw new RuntimeException("Twilio phone number not configured");
    }

    try {
      Message message =
          Message.creator(
                  new PhoneNumber(to), new PhoneNumber(twilioConfig.getPhoneNumber()), messageBody)
              .create();

      log.info("SMS sent successfully to: {} with SID: {}", to, message.getSid());

    } catch (Exception e) {
      log.error("Failed to send SMS to: {}", to, e);
      throw new RuntimeException("Failed to send SMS", e);
    }
  }

  /** Check if SMS service is enabled */
  public boolean isEnabled() {
    return twilioConfig.isEnabled();
  }
}
