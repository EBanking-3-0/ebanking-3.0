package com.ebanking.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.ebanking")
@EnableDiscoveryClient
@EnableFeignClients
public class GraphQLGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(GraphQLGatewayApplication.class, args);
  }
}
