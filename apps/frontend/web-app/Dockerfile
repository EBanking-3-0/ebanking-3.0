# syntax=docker/dockerfile:1

FROM nginx:alpine
COPY apps/frontend/web-app/nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/apps/frontend/web-app/browser /usr/share/nginx/html

# Script to inject env vars into assets/env.js
RUN echo 'echo "window.env = { apiUrl: \"$API_URL\", keycloakUrl: \"$KEYCLOAK_URL\", keycloakRealm: \"$KEYCLOAK_REALM\", keycloakClientId: \"$KEYCLOAK_CLIENT_ID\" };" > /usr/share/nginx/html/assets/env.js && nginx -g "daemon off;"' > /docker-entrypoint.sh && chmod +x /docker-entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/bin/sh", "-c", "/docker-entrypoint.sh"]
