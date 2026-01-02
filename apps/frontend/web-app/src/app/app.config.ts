import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  provideKeycloak,
  includeBearerTokenInterceptor,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  withAutoRefreshToken,
  AutoRefreshTokenService,
  UserActivityService,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,   // ← import this token!
} from 'keycloak-angular';
import { provideApollo } from 'apollo-angular';
import { createUploadLink } from 'apollo-upload-client';
import { InMemoryCache } from '@apollo/client/core';

import { routes } from './app.routes';
import { environment } from '../environments/environment';

// 1. Create your condition(s) — here: add token only to requests going to your backend
const backendCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^http:\/\/localhost:8081(\/.*)?$/i,   // adjust if needed (your GraphQL endpoint)
  // Optional: you can also filter by http methods, e.g.:
  // methods: ['POST', 'GET', 'PUT'],
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),

    // Add the interceptor globally
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),

    // Keycloak setup (no bearer config here!)
    provideKeycloak({
      config: {
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false,               // ← Already good, keep it
        silentCheckSsoFallback: true,          // ← CRITICAL: allow fallback to visible check-sso (redirect) on silent failure
        pkceMethod: 'S256',                    // ← Good security practice
        enableLogging: true,                   // ← ADD THIS → shows detailed Keycloak logs in console
      },
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 60000,
        }),
      ],
    }),

    // Required for auto-refresh
    AutoRefreshTokenService,
    UserActivityService,

    // 2. This is where you define WHEN to add the Bearer token!
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [backendCondition],   // can be multiple conditions
    },

    provideApollo(() => {
      return {
        link: createUploadLink({ uri: 'http://localhost:8081/graphql' }),
        cache: new InMemoryCache(),
      };
    }),
  ],
};
