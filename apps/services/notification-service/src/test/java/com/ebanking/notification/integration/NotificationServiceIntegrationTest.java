package com.ebanking.notification.integration;

import com.ebanking.notification.dto.SendNotificationRequest;
import com.ebanking.notification.entity.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Notification Service
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        mockMvc.perform(get("/api/notifications/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("notification-service"));
    }

    @Test
    void testSendSimpleNotification() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        SendNotificationRequest request = SendNotificationRequest.builder()
            .userId(1L)
            .recipient("test@example.com")
            .notificationType(Notification.NotificationType.GENERIC)
            .channel(Notification.NotificationChannel.EMAIL)
            .subject("Integration Test")
            .content("This is a test notification")
            .build();

        mockMvc.perform(post("/api/notifications/test/simple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.recipient").value("test@example.com"));
    }

    @Test
    void testSendTemplatedEmail() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Map<String, Object> templateData = Map.of(
            "name", "John Doe",
            "amount", "$100.00",
            "date", "2025-12-19"
        );

        mockMvc.perform(post("/api/notifications/test/email/template")
                .param("userId", "1")
                .param("email", "test@example.com")
                .param("templateCode", "welcome-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(templateData)))
            .andExpect(status().isOk());
    }

    @Test
    void testTriggerRetry() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(post("/api/notifications/test/retry"))
            .andExpect(status().isOk())
            .andExpect(content().string("Retry process triggered"));
    }
}