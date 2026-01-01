import { Component, inject, signal, effect } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import Keycloak from 'keycloak-js';
import { KEYCLOAK_EVENT_SIGNAL, KeycloakEventType } from 'keycloak-angular';  // ← Add this import!

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private keycloak = inject(Keycloak);
  private eventSignal = inject(KEYCLOAK_EVENT_SIGNAL);  // ← This is crucial!

  title = 'E-Banking 3.0';
  isLoggedIn = signal(false);
  userProfile = signal<{
    firstName?: string;
    lastName?: string;
    email?: string;
    username?: string;
  } | null>(null);

  constructor() {
    // React to Keycloak events (especially 'Ready' after redirect/login)
    effect(() => {
      const event = this.eventSignal();
      console.log('Keycloak event received:', event?.type);  // Debug – should show 'Ready' after login

      if (event?.type === KeycloakEventType.Ready) {
        console.log('Keycloak READY! Checking auth status...');
        this.updateAuthState();
      }

      // Optional: handle logout event if needed
      if (event?.type === KeycloakEventType.AuthLogout) {
        this.isLoggedIn.set(false);
        this.userProfile.set(null);
      }
    });

    // Very important: also run the check immediately (covers initial load cases)
    setTimeout(() => this.updateAuthState(), 300);  // small delay to let init finish
  }

  private updateAuthState() {
    const authenticated = this.keycloak.authenticated;
    console.log('Auth status check → authenticated:', authenticated);  // Debug

    this.isLoggedIn.set(authenticated);

    if (authenticated) {
      const parsed = this.keycloak.idTokenParsed;
      this.userProfile.set({
        username: parsed?.["preferred_username"],
        email: parsed?.["email"],
        firstName: parsed?.["given_name"],
        lastName: parsed?.["family_name"],
      });
    } else {
      this.userProfile.set(null);
    }
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}
