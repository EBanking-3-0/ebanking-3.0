import { ApplicationConfig, provideZoneChangeDetection, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideApollo } from 'apollo-angular';
import { InMemoryCache } from '@apollo/client/core';
import { createUploadLink } from 'apollo-upload-client';

import { routes } from './app.routes';
import { environment } from '../environments/environment';
import {
  AutoRefreshTokenService,
  createInterceptorCondition, INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  provideKeycloak, UserActivityService, withAutoRefreshToken
} from 'keycloak-angular';

// Condition: add Bearer token only to requests targeting your backend/GraphQL endpoint
const backendCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^http:\/\/localhost:8081(\/.*)?$/i,
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),

    // Global HTTP interceptor for Keycloak Bearer token
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),

    // Keycloak configuration
    provideKeycloak({
      config: {
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false,
        silentCheckSsoFallback: true,
        pkceMethod: 'S256',
        enableLogging: true,
      },
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 60000,
        }),
      ],
    }),

    // Services required for auto-refresh
    AutoRefreshTokenService,
    UserActivityService,

    // Define WHEN the Bearer token interceptor should run
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [backendCondition],
    },

    // Apollo GraphQL client with built-in file upload support
    provideApollo(() => {
      // Use createUploadLink instead of HttpLink for multipart (file upload) support
      const uploadLink = createUploadLink({
        uri: 'http://localhost:8081/graphql',
        withCredentials: true,
      });

      return {
        link: uploadLink,
        cache: new InMemoryCache(),
      };
    }),
  ],
};

