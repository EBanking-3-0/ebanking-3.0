package com.ebanking.notification.mapper;

import com.ebanking.notification.dto.NotificationDTO;
import com.ebanking.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for converting between Notification entity and NotificationDTO. Automatically
 * generated at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {

  /**
   * Convert Notification entity to DTO.
   *
   * @param notification Notification entity
   * @return NotificationDTO
   */
  NotificationDTO toDTO(Notification notification);

  /**
   * Convert NotificationDTO to entity.
   *
   * @param dto NotificationDTO
   * @return Notification entity
   */
  Notification toEntity(NotificationDTO dto);
}
