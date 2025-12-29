package com.ebanking.notification.repository;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for NotificationTemplate entity operations. */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

  /** Find template by unique code */
  Optional<NotificationTemplate> findByTemplateCode(String templateCode);

  /** Find all active templates */
  List<NotificationTemplate> findByActiveTrue();

  /** Find templates by type */
  List<NotificationTemplate> findByTemplateType(Notification.NotificationType templateType);

  /** Find active templates by type and channel */
  Optional<NotificationTemplate> findByTemplateTypeAndChannelAndActiveTrue(
      Notification.NotificationType templateType, Notification.NotificationChannel channel);
}
