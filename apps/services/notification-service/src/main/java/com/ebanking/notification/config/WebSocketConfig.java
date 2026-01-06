package com.ebanking.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notification delivery. Enables STOMP protocol over
 * WebSocket for pub/sub messaging.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable simple in-memory broker for pub/sub
    // Prefix for messages FROM server TO client
    config.enableSimpleBroker("/topic", "/queue");

    // Prefix for messages FROM client TO server
    config.setApplicationDestinationPrefixes("/app");

    // Prefix for user-specific destinations
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket endpoint that clients connect to
    registry
        .addEndpoint("/ws/notifications")
        .setAllowedOriginPatterns("*") // Configure properly for production
        .withSockJS(); // Fallback for browsers that don't support WebSocket

    // Pure WebSocket endpoint (no SockJS)
    registry.addEndpoint("/ws/notifications").setAllowedOriginPatterns("*");
  }
}
