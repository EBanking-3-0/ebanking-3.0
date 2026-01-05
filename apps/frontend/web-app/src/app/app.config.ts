import { ApplicationConfig, inject, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideApollo } from 'apollo-angular';
import { from, InMemoryCache } from '@apollo/client/core';
import UploadHttpLink from 'apollo-upload-client/UploadHttpLink.mjs';
import { setContext } from '@apollo/client/link/context';

import { routes } from './app.routes';
import { environment } from '../environments/environment';

import {
  AutoRefreshTokenService,
  createInterceptorCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  KeycloakService,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';

/**
 * Only attach Bearer token to backend / GraphQL requests
 */
const backendCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^http:\/\/localhost:8081(\/.*)?$/i,
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
      const keycloak = inject(KeycloakService);

      const uploadLink = new UploadHttpLink({
        uri: 'http://localhost:8081/graphql',
        credentials: 'include',
      });

      const authLink = setContext(async (_, { headers }) => {
        let token: string | undefined;

        try {
          // Keycloak may not be initialized yet (silent-check race)
          token = await keycloak.getToken();
        } catch {
          token = undefined;
        }

        return {
          headers: {
            ...headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        };
      });

      return {
        link: from([authLink, uploadLink]),
        cache: new InMemoryCache(),
      };
    }),
  ],
};
