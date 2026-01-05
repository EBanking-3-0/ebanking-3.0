import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakProfile } from 'keycloak-js';

@Component({
  standalone: true,
  imports: [RouterModule],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  isLoggedIn = false;
  userProfile: KeycloakProfile | null = null;

  constructor(
    private readonly keycloak: KeycloakService,
    private readonly router: Router
  ) {}

  async ngOnInit() {
    try {
      this.isLoggedIn = await this.keycloak.isLoggedIn();

      if (this.isLoggedIn) {
        // Use local token data to avoid extra network calls to Keycloak APIs
        const tokenParsed = this.keycloak.getKeycloakInstance().idTokenParsed as any;
        if (tokenParsed) {
          this.userProfile = {
            username: tokenParsed.preferred_username,
            email: tokenParsed.email,
            firstName: tokenParsed.given_name,
            lastName: tokenParsed.family_name,
          };
        }

        if (this.router.url === '/welcome' || this.router.url === '/welcome/') {
          this.router.navigate(['/']);
        }
      } else {
        if (this.router.url === '/') {
          this.router.navigate(['/welcome']);
        }
      }
    } catch (error) {
      console.warn('Authentication check failed:', error);
      this.isLoggedIn = false;
      this.router.navigate(['/welcome']);
    }
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout(window.location.origin);
  }
}
