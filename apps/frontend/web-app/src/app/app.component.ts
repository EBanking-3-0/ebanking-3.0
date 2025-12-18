import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  standalone: true,
})
export class AppComponent implements OnInit {
  title = 'E-Banking 3.0';
  isLoggedIn = false;
  userProfile: any | null = null;

  constructor(private keycloak: KeycloakService) {}

  async ngOnInit() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    if (this.isLoggedIn) {
      // Use idTokenParsed to get user details without an extra network call
      const tokenParsed = this.keycloak.getKeycloakInstance().idTokenParsed;
      this.userProfile = {
        username: tokenParsed?.['preferred_username'],
        email: tokenParsed?.['email'],
        firstName: tokenParsed?.['given_name'],
        lastName: tokenParsed?.['family_name'],
      };
    }
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
  }
}
