package com.ebanking.notification.config;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/** Configuration for Thymeleaf template engine used for email templates. */
@Slf4j
@Configuration
public class TemplateConfig {

  /**
   * Configure template resolver for HTML email templates.
   *
   * @return Spring resource template resolver
   */
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();

    templateResolver.setPrefix("classpath:/templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    templateResolver.setCacheable(true);
    templateResolver.setOrder(1);

    log.info("Thymeleaf template resolver configured");

    return templateResolver;
  }

  /**
   * Configure template engine with custom resolvers.
   *
   * @param templateResolver Template resolver
   * @return Spring template engine
   */
  @Bean
  public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver) {
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();

    templateEngine.setTemplateResolver(templateResolver);
    templateEngine.setEnableSpringELCompiler(true);

    log.info("Thymeleaf template engine configured");

    return templateEngine;
  }
}
