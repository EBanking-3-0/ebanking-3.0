import { Injectable, signal } from '@angular/core';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';
import { UserService } from './user.service';

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  status: string;
  keycloakId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Signal to hold the current user profile
  currentUser = signal<UserProfile | null>(null);

  constructor(private userService: UserService) {
    this.loadUserProfile();
  }

  loadUserProfile() {
    this.userService.getMe()
      .pipe(
        tap(user => {
          console.log('User profile loaded:', user);
          // Map User to UserProfile if needed, but they seem compatible
          this.currentUser.set(user as unknown as UserProfile);
        }),
        catchError(err => {
          console.error('Failed to load user profile', err);
          return of(null);
        })
      )
      .subscribe();
  }

  getCurrentUserId(): string | undefined {
    return this.currentUser()?.id;
  }
  
  isAuthenticated(): boolean {
    return !!this.currentUser();
  }
}
