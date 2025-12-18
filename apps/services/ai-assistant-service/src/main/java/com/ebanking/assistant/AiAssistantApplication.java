package com.ebanking.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {
        "com.ebanking.assistant",
        "com.ebanking.security",
        "com.ebanking.shared.kafka"
})
public class AiAssistantApplication {

  public static void main(String[] args) {
    SpringApplication.run(AiAssistantApplication.class, args);
  }
}
