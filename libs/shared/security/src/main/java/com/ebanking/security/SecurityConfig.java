package com.ebanking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthConverter jwtAuthConverter;

  public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
    this.jwtAuthConverter = jwtAuthConverter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults()) // Keep enabled for Spring to allow OPTIONS
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll() // Allow OPTIONS requests
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll() // Allow auth endpoints (login, register)
                    .requestMatchers("/graphiql/**")
                    .permitAll() // Allow access to GraphiQL UI
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  /**
   * Provide JwtDecoder bean for OAuth2 resource server. Spring Security needs this to validate JWT
   * tokens from the configured issuer.
   *
   * @param jwkSetUri JWK set URI from application configuration
   * @return JwtDecoder instance
   */
  @Bean
  public JwtDecoder jwtDecoder(
      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String jwkSetUri) {
    if (jwkSetUri == null || jwkSetUri.isEmpty()) {
      // Return a no-op decoder if JWK set URI is not configured
      // This prevents startup failures in services that don't use OAuth2
      return token -> null;
    }
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
  }
}
