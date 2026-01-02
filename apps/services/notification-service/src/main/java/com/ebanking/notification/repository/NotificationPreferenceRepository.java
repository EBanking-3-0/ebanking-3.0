package com.ebanking.notification.repository;

import com.ebanking.notification.entity.Notification;
import com.ebanking.notification.entity.NotificationPreference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for NotificationPreference entity operations. */
@Repository
public interface NotificationPreferenceRepository
    extends JpaRepository<NotificationPreference, Long> {

  /** Find preference for a user (one record per user) */
  Optional<NotificationPreference> findByUserId(Long userId);

  /** Delete preference for a user */
  void deleteByUserId(Long userId);

  /** Check if preference exists for user */
  boolean existsByUserId(Long userId);
}
