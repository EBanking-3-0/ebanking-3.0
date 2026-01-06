// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { UserListComponent } from './components/user-list/user-list.component';
import { UserFormComponent } from './components/user-form/user-form.component';
import { AiChatComponent } from './components/ai-chat/ai-chat.component';
import { KycPageComponent } from './components/kyc-page/kyc-page.component';
import { KycWaitingComponent } from './components/kyc-waiting/kyc-waiting';
import { authGuard } from './guards/auth.guard';
import { kycGuard } from './guards/kyc.guard';

export const routes: Routes = [
  // Public / Login only
  { path: 'kyc', component: KycPageComponent, canMatch: [authGuard] },
  { path: 'kyc/waiting', component: KycWaitingComponent, canMatch: [authGuard] },

  // Protected: Must be logged in
  {
    path: '',
    canMatch: [authGuard],
    children: [
      { path: '', component: UserListComponent },
      { path: 'users/new', component: UserFormComponent },
      { path: 'users/edit/:id', component: UserFormComponent },
    ],
  },

  // Fully Protected: Logged in + KYC Approved
  {
    path: 'ai-chat',
    component: AiChatComponent,
    canMatch: [kycGuard],
  },

  // Fallback
  { path: '**', redirectTo: '' },
];
