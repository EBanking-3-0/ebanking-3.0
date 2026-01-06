package com.ebanking.user.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

  @Value("${keycloak.auth-server-url}")
  private String serverUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm("master") // admin realm
        .username("admin")
        .password("admin") // change in prod to client credentials grant
        .clientId("admin-cli")
        .grantType("password")
        .build();
  }
}
