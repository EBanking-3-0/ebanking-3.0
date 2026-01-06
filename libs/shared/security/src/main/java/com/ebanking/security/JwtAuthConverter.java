package com.ebanking.security;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
      new JwtGrantedAuthoritiesConverter();

  private final String principleAttribute = "preferred_username";

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    System.out.println("DEBUG: JwtAuthConverter.convert called for subject: " + jwt.getSubject());
    try {
      Collection<GrantedAuthority> authorities =
          Stream.concat(
                  jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                  extractResourceRoles(jwt).stream())
              .collect(Collectors.toSet());

      return new JwtAuthenticationToken(jwt, authorities, getPrincipleClaimName(jwt));
    } catch (Exception e) {
      System.err.println("DEBUG: Error in JwtAuthConverter: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  private String getPrincipleClaimName(Jwt jwt) {
    String claimName = JwtClaimNames.SUB;
    if (principleAttribute != null) {
      claimName = principleAttribute;
    }
    return jwt.getClaim(claimName);
  }

  private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
    Map<String, Object> realmAccess;
    Collection<String> roles;

    if (jwt.getClaim("realm_access") == null) {
      return Set.of();
    }
    realmAccess = jwt.getClaim("realm_access");
    roles = (Collection<String>) realmAccess.get("roles");

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }
}
