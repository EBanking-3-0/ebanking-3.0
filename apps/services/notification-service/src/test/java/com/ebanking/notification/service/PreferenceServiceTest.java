package com.ebanking.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.entity.NotificationPreference;
import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.enums.NotificationType;
import com.ebanking.notification.repository.NotificationPreferenceRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for PreferenceService. */
@ExtendWith(MockitoExtension.class)
@DisplayName("PreferenceService Unit Tests")
class PreferenceServiceTest {

  @Mock private NotificationPreferenceRepository preferenceRepository;

  @Mock private com.ebanking.notification.mapper.NotificationPreferenceMapper preferenceMapper;

  private PreferenceService preferenceService;

  @BeforeEach
  void setUp() {
    preferenceService = new PreferenceService(preferenceRepository, preferenceMapper);

    // Setup mapper to return DTOs from entities (lenient as not all tests need it)
    lenient()
        .when(preferenceMapper.toDTO(any(NotificationPreference.class)))
        .thenAnswer(
            invocation -> {
              NotificationPreference preference = invocation.getArgument(0);
              return NotificationPreferenceDTO.builder()
                  .id(preference.getId())
                  .userId(preference.getUserId())
                  .notificationType(preference.getNotificationType())
                  .channel(preference.getChannel())
                  .enabled(preference.getEnabled())
                  .build();
            });
  }

  @Test
  @DisplayName("Should get enabled channels for user and type")
  void testGetEnabledChannels() {
    // Arrange
    Long userId = 1L;
    NotificationType type = NotificationType.TRANSACTION;

    List<NotificationChannel> enabledChannels =
        List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP);

    when(preferenceRepository.findEnabledChannels(userId, type)).thenReturn(enabledChannels);

    // Act
    List<NotificationChannel> result = preferenceService.getEnabledChannels(userId, type);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(NotificationChannel.EMAIL));
    assertTrue(result.contains(NotificationChannel.IN_APP));
    assertFalse(result.contains(NotificationChannel.SMS));
    verify(preferenceRepository, times(1)).findEnabledChannels(userId, type);
  }

  @Test
  @DisplayName("Should return default channels when no preferences set")
  void testGetEnabledChannelsWithDefaults() {
    // Arrange
    Long userId = 1L;
    NotificationType type = NotificationType.WELCOME;

    when(preferenceRepository.findEnabledChannels(userId, type)).thenReturn(List.of());

    // Act
    List<NotificationChannel> result = preferenceService.getEnabledChannels(userId, type);

    // Assert
    assertNotNull(result);
    assertTrue(result.size() > 0);
  }

  @Test
  @DisplayName("Should check if channel is enabled")
  void testIsChannelEnabled() {
    // Arrange
    Long userId = 1L;
    NotificationType type = NotificationType.TRANSACTION;
    NotificationChannel channel = NotificationChannel.EMAIL;

    when(preferenceRepository.isChannelEnabled(userId, type, channel)).thenReturn(true);

    // Act
    boolean result = preferenceService.isChannelEnabled(userId, type, channel);

    // Assert
    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when preference not found")
  void testIsChannelEnabledNotFound() {
    // Arrange
    Long userId = 1L;
    NotificationType type = NotificationType.TRANSACTION;
    NotificationChannel channel = NotificationChannel.PUSH;

    // Act
    boolean result = preferenceService.isChannelEnabled(userId, type, channel);

    // Assert
    assertFalse(result);
  }

  @Test
  @DisplayName("Should save user preference")
  void testSavePreference() {
    // Arrange
    NotificationPreferenceDTO dto =
        NotificationPreferenceDTO.builder()
            .userId(1L)
            .notificationType(NotificationType.TRANSACTION)
            .channel(NotificationChannel.EMAIL)
            .enabled(true)
            .build();

    NotificationPreference preference =
        NotificationPreference.builder()
            .userId(1L)
            .notificationType(NotificationType.TRANSACTION)
            .channel(NotificationChannel.EMAIL)
            .enabled(true)
            .build();

    when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);

    // Act
    NotificationPreferenceDTO result = preferenceService.savePreference(dto);

    // Assert
    assertNotNull(result);
    assertEquals(dto.getUserId(), result.getUserId());
    assertEquals(dto.getChannel(), result.getChannel());
    assertTrue(result.getEnabled());
  }

  @Test
  @DisplayName("Should initialize default preferences for new user")
  void testInitializeDefaultPreferences() {
    // Arrange
    Long userId = 1L;

    when(preferenceRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    preferenceService.initializeDefaultPreferences(userId);

    // Assert
    verify(preferenceRepository, times(1)).saveAll(anyList());
  }
}
