FROM quay.io/keycloak/keycloak:26.4.5

# Copy custom providers (plugins)
# Important: Place your .jar files in tools/docker/keycloak/providers/
COPY providers/*.jar /opt/keycloak/providers/

# Build Keycloak to register the provider and optimize for production
RUN /opt/keycloak/bin/kc.sh build
