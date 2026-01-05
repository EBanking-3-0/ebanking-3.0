package com.ebanking.legacyadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
    scanBasePackages = "com.ebanking",
    exclude = {
      org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
      org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
          .class,
      org.springframework.boot.actuate.autoconfigure.security.servlet
          .ManagementWebSecurityAutoConfiguration.class,
      org.springframework.boot.autoconfigure.security.oauth2.resource.servlet
          .OAuth2ResourceServerAutoConfiguration.class
    })
@EnableDiscoveryClient
@ComponentScan(
    basePackages = "com.ebanking",
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = com.ebanking.security.SecurityConfig.class))
public class LegacyAdapterServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(LegacyAdapterServiceApplication.class, args);
  }
}
