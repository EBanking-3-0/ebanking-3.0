package com.ebanking.notification.service;

import com.ebanking.notification.config.TemplateConfig;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Service for rendering notification templates. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

  private final TemplateEngine templateEngine;
  private final TemplateConfig templateConfig;

  /**
   * Render a Thymeleaf template with given data
   *
   * @param templateName Name of the template file (without .html extension)
   * @param variables Variables to be used in the template
   * @return Rendered HTML content
   */
  public String renderTemplate(String templateName, Map<String, Object> variables) {
    try {
      Context context = new Context();
      if (variables != null) {
        context.setVariables(variables);
      }

      // Use configured base path for template resolution
      String fullTemplatePath = templateName;
      if (templateConfig.getBasePath() != null
          && !templateName.startsWith(templateConfig.getBasePath())) {
        fullTemplatePath = templateConfig.getBasePath() + templateName;
      }

      String rendered = templateEngine.process(fullTemplatePath, context);
      log.debug(
          "Template {} rendered successfully using base path: {}",
          templateName,
          templateConfig.getBasePath());
      return rendered;

    } catch (Exception e) {
      log.error(
          "Failed to render template: {} with base path: {}",
          templateName,
          templateConfig.getBasePath(),
          e);
      throw new RuntimeException("Failed to render template: " + templateName, e);
    }
  }

  /** Render a simple text template by replacing placeholders Format: {{variableName}} */
  public String renderSimpleTemplate(String template, Map<String, Object> variables) {
    if (variables == null || variables.isEmpty()) {
      return template;
    }

    String result = template;
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      String value = entry.getValue() != null ? entry.getValue().toString() : "";
      result = result.replace(placeholder, value);
    }

    return result;
  }
}
