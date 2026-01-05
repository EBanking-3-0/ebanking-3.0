package com.ebanking.notification.service;

import com.ebanking.notification.entity.NotificationTemplate;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.exception.TemplateException;
import com.ebanking.notification.repository.NotificationTemplateRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service for managing and rendering notification templates. Supports both database templates and
 * file-based Thymeleaf templates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final NotificationTemplateRepository templateRepository;
  private final TemplateEngine templateEngine;
  private final ResourceLoader resourceLoader;

  @Value("${notification.template.base-path:notifications/}")
  private String templateBasePath;

  @Value("${notification.template.default-locale:en}")
  private String defaultLocale;

  /**
   * Render a template with variables.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @param variables Template variables
   * @param locale User's preferred locale
   * @return Rendered content
   */
  public String renderTemplate(
      NotificationType type,
      NotificationChannel channel,
      Map<String, Object> variables,
      String locale) {

    try {
      // Try to get template from database first
      NotificationTemplate template = getTemplate(type, channel, locale);

      if (template != null) {
        return renderDatabaseTemplate(template, variables);
      }

      // Fall back to file-based template
      return renderFileTemplate(type, channel, variables);

    } catch (Exception e) {
      log.error("Error rendering template for type: {}, channel: {}", type, channel, e);
      throw new TemplateException("Failed to render template: " + e.getMessage(), e);
    }
  }

  /**
   * Render a template using default locale.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @param variables Template variables
   * @return Rendered content
   */
  public String renderTemplate(
      NotificationType type, NotificationChannel channel, Map<String, Object> variables) {
    return renderTemplate(type, channel, variables, defaultLocale);
  }

  /**
   * Get subject for a notification type.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @param variables Template variables (for dynamic subjects)
   * @return Subject string
   */
  public String getSubject(
      NotificationType type, NotificationChannel channel, Map<String, Object> variables) {

    NotificationTemplate template = getTemplate(type, channel, defaultLocale);

    if (template != null && template.getSubject() != null) {
      // Render subject as a simple template
      Context context = new Context();
      context.setVariables(variables);
      return templateEngine.process(template.getSubject(), context);
    }

    // Default subjects
    return getDefaultSubject(type);
  }

  /**
   * Get template from database or file system.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @param locale Locale
   * @return Template or null if not found
   */
  private NotificationTemplate getTemplate(
      NotificationType type, NotificationChannel channel, String locale) {

    // Try to get template with specific locale
    return templateRepository
        .findByNotificationTypeAndChannelAndLocaleAndActiveTrue(type, channel, locale)
        .or(() -> templateRepository.findDefaultTemplate(type, channel))
        .orElse(null);
  }

  /**
   * Render template content from database.
   *
   * @param template Template entity
   * @param variables Template variables
   * @return Rendered content
   */
  private String renderDatabaseTemplate(
      NotificationTemplate template, Map<String, Object> variables) {

    log.debug("Rendering database template: {}", template.getName());

    Context context = new Context();
    context.setVariables(variables);

    return templateEngine.process(template.getContent(), context);
  }

  /**
   * Render template from file system using Thymeleaf.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @param variables Template variables
   * @return Rendered content
   */
  private String renderFileTemplate(
      NotificationType type, NotificationChannel channel, Map<String, Object> variables) {

    String templateName = getTemplateFileName(type, channel);
    log.debug("Rendering file template: {}", templateName);

    try {
      // For email templates, use Thymeleaf
      if (channel == NotificationChannel.EMAIL) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
      }

      // For other channels, load plain text template
      String templatePath = "classpath:templates/" + templateBasePath + templateName;
      Resource resource = resourceLoader.getResource(templatePath);

      if (!resource.exists()) {
        throw new TemplateException("Template file not found: " + templatePath);
      }

      String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

      // Simple variable replacement for non-HTML templates
      return replaceVariables(template, variables);

    } catch (IOException e) {
      throw new TemplateException("Failed to load template file: " + e.getMessage(), e);
    }
  }

  /**
   * Get template file name based on type and channel.
   *
   * @param type Notification type
   * @param channel Notification channel
   * @return Template file name
   */
  private String getTemplateFileName(NotificationType type, NotificationChannel channel) {
    String typeName = type.name().toLowerCase().replace("_", "-");
    String channelName = channel.name().toLowerCase();
    return templateBasePath + typeName + "-" + channelName;
  }

  /**
   * Simple variable replacement for non-HTML templates.
   *
   * @param template Template string
   * @param variables Variables to replace
   * @return Template with variables replaced
   */
  private String replaceVariables(String template, Map<String, Object> variables) {
    String result = template;

    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      result = result.replace(placeholder, String.valueOf(entry.getValue()));
    }

    return result;
  }

  /**
   * Get default subject for notification type.
   *
   * @param type Notification type
   * @return Default subject
   */
  private String getDefaultSubject(NotificationType type) {
    return switch (type) {
      case WELCOME -> "Welcome to E-Banking!";
      case TRANSACTION -> "Transaction Notification";
      case FRAUD_ALERT -> "⚠️ URGENT: Security Alert";
      case ALERT -> "Account Alert";
      case ACCOUNT_CREATED -> "New Account Created";
      case PAYMENT_FAILED -> "Payment Failed";
      case LOGIN -> "New Login Detected";
      case MFA -> "Verification Code";
      case CRYPTO_TRADE -> "Crypto Trade Executed";
      case SYSTEM -> "System Notification";
      default -> "E-Banking Notification";
    };
  }
}
