FROM quay.io/keycloak/keycloak:26.3

# Copy custom providers (plugins)
COPY providers/*.jar /opt/keycloak/providers/

# Build Keycloak to register the provider
RUN /opt/keycloak/bin/kc.sh build
