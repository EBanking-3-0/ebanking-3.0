package com.ebanking.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ebanking.notification.config.TemplateConfig;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Unit tests for TemplateService */
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

  @Mock private TemplateEngine templateEngine;

  @Mock private TemplateConfig templateConfig;

  private TemplateService templateService;

  @BeforeEach
  void setUp() {
    templateService = new TemplateService(templateEngine, templateConfig);
  }

  @Test
  void testRenderTemplateWithBasePath() {
    // Given
    String templateName = "welcome-email";
    String basePath = "templates/";
    Map<String, Object> variables = Map.of("name", "John Doe");
    String expectedContent = "<html>Welcome John Doe</html>";

    when(templateConfig.getBasePath()).thenReturn(basePath);
    when(templateEngine.process(eq("templates/welcome-email"), any(Context.class)))
        .thenReturn(expectedContent);

    // When
    String result = templateService.renderTemplate(templateName, variables);

    // Then
    assertEquals(expectedContent, result);
    verify(templateEngine).process("templates/welcome-email", any(Context.class));
  }

  @Test
  void testRenderTemplateWithoutBasePath() {
    // Given
    String templateName = "welcome-email";
    Map<String, Object> variables = Map.of("name", "John Doe");
    String expectedContent = "<html>Welcome John Doe</html>";

    when(templateConfig.getBasePath()).thenReturn(null);
    when(templateEngine.process(eq("welcome-email"), any(Context.class)))
        .thenReturn(expectedContent);

    // When
    String result = templateService.renderTemplate(templateName, variables);

    // Then
    assertEquals(expectedContent, result);
    verify(templateEngine).process("welcome-email", any(Context.class));
  }

  @Test
  void testRenderSimpleTemplate() {
    // Given
    String template = "Hello {{name}}, your balance is {{balance}}";
    Map<String, Object> variables =
        Map.of(
            "name", "John Doe",
            "balance", "$100.00");

    // When
    String result = templateService.renderSimpleTemplate(template, variables);

    // Then
    assertEquals("Hello John Doe, your balance is $100.00", result);
  }

  @Test
  void testRenderSimpleTemplateWithNullVariables() {
    // Given
    String template = "Hello World";

    // When
    String result = templateService.renderSimpleTemplate(template, null);

    // Then
    assertEquals("Hello World", result);
  }

  @Test
  void testRenderSimpleTemplateWithMissingVariable() {
    // Given
    String template = "Hello {{name}}, your balance is {{balance}}";
    Map<String, Object> variables = Map.of("name", "John Doe");

    // When
    String result = templateService.renderSimpleTemplate(template, variables);

    // Then
    assertEquals("Hello John Doe, your balance is ", result);
  }
}
