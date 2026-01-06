import { ApplicationConfig, inject, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideApollo } from 'apollo-angular';
import { from, InMemoryCache, ApolloLink } from '@apollo/client/core';
import UploadHttpLink from 'apollo-upload-client/UploadHttpLink.mjs';
import { setContext } from '@apollo/client/link/context';
import Keycloak from 'keycloak-js';

import { routes } from './app.routes';
import { environment } from '../environments/environment';

import {
  AutoRefreshTokenService,
  createInterceptorCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';

/**
 * Only attach Bearer token to backend / GraphQL requests
 */
const backendCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: new RegExp(`^${environment.apiRestUrl}|${environment.apiUrl}`),
});

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),

    /**
     * Global HTTP interceptor (REST calls)
     */
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),

    /**
     * Keycloak initialization
     */
    provideKeycloak({
      config: {
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId,
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri:
          window.location.origin + '/assets/silent-check-sso.html',
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

    /**
     * Required services for auto-refresh
     */
    AutoRefreshTokenService,
    UserActivityService,

    /**
     * Define when the Bearer token interceptor runs
     */
    {
      provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
      useValue: [backendCondition],
    },

    /**
     * Apollo GraphQL configuration (SAFE with Keycloak + silent SSO)
     */
    provideApollo(() => {
      console.log('Initializing Apollo Client...');
      try {
        const keycloak = inject(Keycloak);

        const uploadLink = new (UploadHttpLink as any)({
          uri: environment.apiUrl,
          credentials: 'include',
        }) as unknown as ApolloLink;

        const authLink = setContext(async (_, { headers }) => {
          let token: string | undefined;

          try {
            // Ensure token is valid (refresh if needed, minValidity = 30s)
            await keycloak.updateToken(30);
            token = keycloak.token;
          } catch (error) {
            // Token update failed or not logged in
            token = undefined;
          }

          return {
            headers: {
              ...headers,
              ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
          };
        });

        const options = {
          link: from([authLink, uploadLink]),
          cache: new InMemoryCache(),
        };

        console.log('Apollo Client options created:', options);
        return options;
      } catch (error) {
        console.error('Error initializing Apollo Client:', error);
        throw error;
      }
    }),
  ],
};
