package com.ebanking.notification.service;

import com.ebanking.notification.enums.NotificationChannel;
import com.ebanking.notification.exception.NotificationException;
import com.ebanking.notification.strategy.NotificationStrategy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for getting the appropriate notification strategy based on channel. Uses Spring's
 * dependency injection to automatically discover all NotificationStrategy implementations.
 */
@Slf4j
@Component
public class NotificationStrategyFactory {

  private final Map<NotificationChannel, NotificationStrategy> strategies;

  /**
   * Constructor that autowires all NotificationStrategy beans and maps them by channel.
   *
   * @param strategyList List of all NotificationStrategy implementations
   */
  public NotificationStrategyFactory(List<NotificationStrategy> strategyList) {
    this.strategies =
        strategyList.stream()
            .collect(
                Collectors.toMap(
                    strategy -> NotificationChannel.valueOf(strategy.getChannelName()),
                    strategy -> strategy));

    log.info(
        "Initialized NotificationStrategyFactory with {} strategies: {}",
        strategies.size(),
        strategies.keySet());
  }

  /**
   * Get the notification strategy for a specific channel.
   *
   * @param channel Notification channel
   * @return NotificationStrategy for the channel
   * @throws NotificationException if no strategy is found for the channel
   */
  public NotificationStrategy getStrategy(NotificationChannel channel) {
    NotificationStrategy strategy = strategies.get(channel);

    if (strategy == null) {
      throw new NotificationException("No notification strategy found for channel: " + channel);
    }

    return strategy;
  }

  /**
   * Check if a strategy is available for a channel.
   *
   * @param channel Notification channel
   * @return true if strategy is available
   */
  public boolean hasStrategy(NotificationChannel channel) {
    return strategies.containsKey(channel);
  }

  /**
   * Get all available channels.
   *
   * @return Set of available channels
   */
  public List<NotificationChannel> getAvailableChannels() {
    return List.copyOf(strategies.keySet());
  }
}
