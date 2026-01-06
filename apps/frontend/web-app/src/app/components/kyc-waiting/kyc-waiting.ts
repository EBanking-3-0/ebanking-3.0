// src/app/components/kyc-waiting/kyc-waiting.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-kyc-waiting',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="waiting-container">
      <h1>⏳ KYC Under Review</h1>
      <p>Your identity verification is being processed.</p>
      <p>This usually takes 1–3 business days.</p>
      <p>You will receive an email once your account is approved.</p>

      <div class="status-box">
        <strong>Current Status:</strong> Pending Review
      </div>

      <button routerLink="/kyc" class="btn-primary">Check KYC Status</button>
      <button (click)="logout()" class="btn-secondary">Logout</button>
    </div>
  `,
  styles: [`
    .waiting-container {
      max-width: 600px;
      margin: 60px auto;
      text-align: center;
      padding: 40px;
      font-family: system-ui, sans-serif;
    }
    h1 { font-size: 2.5rem; margin-bottom: 20px; }
    p { font-size: 1.2rem; margin: 15px 0; color: #444; }
    .status-box {
      background: #fff3cd;
      color: #856404;
      padding: 15px;
      border-radius: 8px;
      margin: 30px 0;
      font-size: 1.3rem;
      border: 1px solid #ffeaa7;
    }
    .btn-primary, .btn-secondary {
      padding: 12px 24px;
      margin: 10px;
      border: none;
      border-radius: 6px;
      font-size: 1.1rem;
      cursor: pointer;
    }
    .btn-primary {
      background: #1976d2;
      color: white;
    }
    .btn-secondary {
      background: #6c757d;
      color: white;
    }
  `]
})
export class KycWaitingComponent {
  logout() {
    // Assuming you have Keycloak injected in root or a service
    // Or just redirect to logout endpoint
    window.location.href = '/'; // or use Keycloak logout
  }
}
