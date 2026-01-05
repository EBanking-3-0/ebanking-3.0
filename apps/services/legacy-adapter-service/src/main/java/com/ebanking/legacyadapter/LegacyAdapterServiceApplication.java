package com.ebanking.legacyadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = "com.ebanking")
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
