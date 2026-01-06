package com.ebanking.notification.mapper;

import com.ebanking.notification.dto.NotificationPreferenceDTO;
import com.ebanking.notification.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between NotificationPreference entity and
 * NotificationPreferenceDTO. Automatically generated at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationPreferenceMapper {

  /**
   * Convert NotificationPreference entity to DTO.
   *
   * @param preference NotificationPreference entity
   * @return NotificationPreferenceDTO
   */
  NotificationPreferenceDTO toDTO(NotificationPreference preference);

  /**
   * Convert NotificationPreferenceDTO to entity.
   *
   * @param dto NotificationPreferenceDTO
   * @return NotificationPreference entity
   */
  NotificationPreference toEntity(NotificationPreferenceDTO dto);
}
