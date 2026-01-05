import { Routes } from '@angular/router';
import { UserListComponent } from './components/user-list/user-list.component';
import { UserFormComponent } from './components/user-form/user-form.component';
import { AiChatComponent } from './components/ai-chat/ai-chat.component';
import { KycPageComponent } from './components/kyc-page/kyc-page.component';
import { LandingPageComponent } from './components/landing-page/landing-page';

export const routes: Routes = [
  { path: 'welcome', component: LandingPageComponent },
  { path: '', component: UserListComponent },
  { path: 'users/new', component: UserFormComponent },
  { path: 'users/edit/:id', component: UserFormComponent },
  { path: 'ai-chat', component: AiChatComponent },
  { path: 'kyc', component: KycPageComponent },
  { path: '**', redirectTo: '' },
];
