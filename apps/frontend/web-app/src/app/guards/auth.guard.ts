// src/app/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanMatch, Router } from '@angular/router';
import Keycloak from 'keycloak-js';

export const authGuard: () => (boolean) = () => {
  const keycloak = inject(Keycloak);
  const router = inject(Router);

  if (keycloak.authenticated) {
    return true;
  }

  // Redirect to Keycloak login with return URL
  keycloak.login({
    redirectUri: window.location.origin + router.url,
  });
  return false;
};
