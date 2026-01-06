package com.ebanking.notification.service;

import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.mapper.NotificationPreferenceMapper;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user notification preferences. Handles CRUD operations and preference
 * lookups.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceService {

  private final NotificationPreferenceRepository preferenceRepository;
  private final NotificationPreferenceMapper preferenceMapper;

  /**
   * Get all preferences for a user.
   *
   * @param userId User ID
   * @return List of preference DTOs
   */
  public List<NotificationPreferenceDTO> getUserPreferences(Long userId) {
    log.debug("Fetching preferences for user: {}", userId);

    return preferenceRepository.findByUserId(userId).stream().map(this::toDTO).toList();
  }

  /**
   * Get preferences for a specific notification type.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @return List of preference DTOs
   */
  public List<NotificationPreferenceDTO> getUserPreferencesByType(
      Long userId, NotificationType notificationType) {

    log.debug("Fetching preferences for user: {} and type: {}", userId, notificationType);

    return preferenceRepository.findByUserIdAndNotificationType(userId, notificationType).stream()
        .map(this::toDTO)
        .toList();
  }

  /**
   * Get enabled channels for a user and notification type. This is the main method used to
   * determine which channels to send notifications through.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @return List of enabled channels
   */
  public List<NotificationChannel> getEnabledChannels(
      Long userId, NotificationType notificationType) {

    List<NotificationChannel> channels =
        preferenceRepository.findEnabledChannels(userId, notificationType);

    log.debug(
        "User: {} has {} enabled channels for type: {}", userId, channels.size(), notificationType);

    // If no preferences set, return default channels
    if (channels.isEmpty()) {
      return getDefaultChannels(notificationType);
    }

    return channels;
  }

  /**
   * Check if a specific channel is enabled for a user and notification type.
   *
   * @param userId User ID
   * @param notificationType Notification type
   * @param channel Notification channel
   * @return true if enabled
   */
  public boolean isChannelEnabled(
      Long userId, NotificationType notificationType, NotificationChannel channel) {

    return preferenceRepository.isChannelEnabled(userId, notificationType, channel);
  }

  /**
   * Create or update a preference.
   *
   * @param dto Preference DTO
   * @return Updated preference DTO
   */
  @Transactional
  public NotificationPreferenceDTO savePreference(NotificationPreferenceDTO dto) {
    log.info(
        "Saving preference for user: {}, type: {}, channel: {}",
        dto.getUserId(),
        dto.getNotificationType(),
        dto.getChannel());

    // Check if preference already exists
    NotificationPreference preference =
        preferenceRepository
            .findByUserIdAndNotificationTypeAndChannel(
                dto.getUserId(), dto.getNotificationType(), dto.getChannel())
            .orElse(new NotificationPreference());

    // Update fields
    preference.setUserId(dto.getUserId());
    preference.setNotificationType(dto.getNotificationType());
    preference.setChannel(dto.getChannel());
    preference.setEnabled(dto.getEnabled());

    NotificationPreference saved = preferenceRepository.save(preference);

    return toDTO(saved);
  }

  /**
   * Save multiple preferences at once.
   *
   * @param preferences List of preference DTOs
   * @return List of saved preference DTOs
   */
  @Transactional
  public List<NotificationPreferenceDTO> savePreferences(
      List<NotificationPreferenceDTO> preferences) {

    log.info("Saving {} preferences", preferences.size());

    return preferences.stream().map(this::savePreference).toList();
  }

  /**
   * Delete a preference.
   *
   * @param preferenceId Preference ID
   */
  @Transactional
  public void deletePreference(Long preferenceId) {
    log.info("Deleting preference: {}", preferenceId);
    preferenceRepository.deleteById(preferenceId);
  }

  /**
   * Delete all preferences for a user.
   *
   * @param userId User ID
   */
  @Transactional
  public void deleteUserPreferences(Long userId) {
    log.info("Deleting all preferences for user: {}", userId);
    preferenceRepository.deleteByUserId(userId);
  }

  /**
   * Initialize default preferences for a new user.
   *
   * @param userId User ID
   */
  @Transactional
  public void initializeDefaultPreferences(Long userId) {
    log.info("Initializing default preferences for user: {}", userId);

    // Create default preferences for important notification types
    List<NotificationPreference> defaults =
        List.of(
            // Welcome - Email and In-App
            createPreference(userId, NotificationType.WELCOME, NotificationChannel.EMAIL, true),
            createPreference(userId, NotificationType.WELCOME, NotificationChannel.IN_APP, true),

            // Transaction - All channels
            createPreference(userId, NotificationType.TRANSACTION, NotificationChannel.EMAIL, true),
            createPreference(
                userId, NotificationType.TRANSACTION, NotificationChannel.IN_APP, true),
            createPreference(userId, NotificationType.TRANSACTION, NotificationChannel.SMS, false),

            // Fraud alerts - All channels enabled
            createPreference(userId, NotificationType.FRAUD_ALERT, NotificationChannel.EMAIL, true),
            createPreference(
                userId, NotificationType.FRAUD_ALERT, NotificationChannel.IN_APP, true),
            createPreference(userId, NotificationType.FRAUD_ALERT, NotificationChannel.SMS, true),

            // Alerts - Email and In-App
            createPreference(userId, NotificationType.ALERT, NotificationChannel.EMAIL, true),
            createPreference(userId, NotificationType.ALERT, NotificationChannel.IN_APP, true),

            // Login - In-App only by default
            createPreference(userId, NotificationType.LOGIN, NotificationChannel.EMAIL, false),
            createPreference(userId, NotificationType.LOGIN, NotificationChannel.IN_APP, true));

    preferenceRepository.saveAll(defaults);
    log.info("Initialized {} default preferences for user: {}", defaults.size(), userId);
  }

  /**
   * Get default channels for a notification type when no preferences are set.
   *
   * @param type Notification type
   * @return List of default channels
   */
  private List<NotificationChannel> getDefaultChannels(NotificationType type) {
    return switch (type) {
      case FRAUD_ALERT ->
          List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP, NotificationChannel.SMS);
      case TRANSACTION, ALERT, ACCOUNT_CREATED ->
          List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);
      case WELCOME, PAYMENT_FAILED, CRYPTO_TRADE ->
          List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);
      default -> List.of(NotificationChannel.IN_APP);
    };
  }

  /**
   * Create a preference entity.
   *
   * @param userId User ID
   * @param type Notification type
   * @param channel Notification channel
   * @param enabled Enabled status
   * @return NotificationPreference entity
   */
  private NotificationPreference createPreference(
      Long userId, NotificationType type, NotificationChannel channel, boolean enabled) {
    return NotificationPreference.builder()
        .userId(userId)
        .notificationType(type)
        .channel(channel)
        .enabled(enabled)
        .build();
  }

  /**
   * Convert entity to DTO using MapStruct mapper.
   *
   * @param preference Preference entity
   * @return Preference DTO
   */
  private NotificationPreferenceDTO toDTO(NotificationPreference preference) {
    return preferenceMapper.toDTO(preference);
  }
}
