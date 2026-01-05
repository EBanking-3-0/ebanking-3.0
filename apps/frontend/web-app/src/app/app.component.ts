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
    this.isLoggedIn = await this.keycloak.isLoggedIn();

    if (this.isLoggedIn) {
      this.userProfile = await this.keycloak.loadUserProfile();
      if (this.router.url === '/welcome') {
        this.router.navigate(['/']);
      }
    } else {
      if (this.router.url === '/') {
        this.router.navigate(['/welcome']);
      }
    }
  }

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout(window.location.origin);
  }
}
