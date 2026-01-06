// src/app/guards/kyc.guard.ts
import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { gql } from 'apollo-angular';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import Keycloak from 'keycloak-js';

const GET_KYC_STATUS = gql`
  query Me {
    me {
      kycStatus
    }
  }
`;

export const kycGuard: CanMatchFn = () => {
  const apollo = inject(Apollo);
  const router = inject(Router);
  const keycloak = inject(Keycloak);

  // If not logged in → trigger Keycloak login
  if (!keycloak.authenticated) {
    keycloak.login({
      redirectUri: window.location.origin + router.url,
    });
    return false;
  }

  // CRITICAL FIXES:
  // 1. Use query() instead of watchQuery() → one-time fresh request
  // 2. fetchPolicy: 'network-only' → bypass cache completely
  // 3. Clear cache for this query before running (extra safety after login)
  apollo.client.cache.evict({ fieldName: 'me' });
  apollo.client.cache.gc();

  return apollo
    .query<{ me: { kycStatus: string | null } }>({
      query: GET_KYC_STATUS,
      fetchPolicy: 'network-only', // ← forces new request with current headers
    })
    .pipe(
      map((result) => {
        const status = result.data?.me?.kycStatus;
        console.log('KYC Guard - Fresh result:', result.data);
        console.log('KYC Status:', status);

        if (status === 'VERIFIED') {
          return true;
        }

        if (['PENDING_REVIEW', 'MORE_INFO_NEEDED', 'REJECTED'].includes(status ?? '')) {
          return router.createUrlTree(['/kyc/waiting']);
        }

        // null, undefined, or any other → go to KYC form
        return router.createUrlTree(['/kyc']);
      }),
      catchError((error) => {
        console.error('KYC Guard - Query error:', error);
        return of(router.createUrlTree(['/kyc']));
      })
    );
};
