import { Component, inject, signal, effect } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import Keycloak from 'keycloak-js';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType } from 'keycloak-angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private keycloak = inject(Keycloak);
  private eventSignal = inject(KEYCLOAK_EVENT_SIGNAL);
  private router = inject(Router);

  title = 'E-Banking 3.0';
  isLoggedIn = signal(false);
  userProfile = signal<{
    firstName?: string;
    lastName?: string;
    email?: string;
    username?: string;
  } | null>(null);

  constructor() {
    // React to Keycloak events
    effect(() => {
      const event = this.eventSignal();
      if (event?.type === KeycloakEventType.Ready) {
        this.updateAuthState();
      }

      if (event?.type === KeycloakEventType.AuthLogout) {
        this.isLoggedIn.set(false);
        this.userProfile.set(null);
      }
    });

    // Initial check
    setTimeout(() => this.updateAuthState(), 300);
  }

  private updateAuthState() {
    const authenticated = this.keycloak.authenticated || false;
    this.isLoggedIn.set(authenticated);

    if (authenticated) {
      const parsed = this.keycloak.idTokenParsed as any;
      if (parsed) {
        this.userProfile.set({
          username: parsed.preferred_username,
          email: parsed.email,
          firstName: parsed.given_name,
          lastName: parsed.family_name,
        });
      }

      // Handle landing page redirection
      if (this.router.url === '/welcome' || this.router.url === '/welcome/') {
        this.router.navigate(['/']);
      }
    } else {
      this.userProfile.set(null);
      // Redirect to landing page if on protected root
      if (this.router.url === '/') {
        this.router.navigate(['/welcome']);
      }
    }
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}
