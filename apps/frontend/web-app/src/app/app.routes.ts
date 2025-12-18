import { Routes } from '@angular/router';
import { UserListComponent } from './components/user-list/user-list.component';
import { UserFormComponent } from './components/user-form/user-form.component';
import { AiChatComponent } from './components/ai-chat/ai-chat.component';

export const routes: Routes = [
  { path: '', component: UserListComponent },
  { path: 'users/new', component: UserFormComponent },
  { path: 'users/edit/:id', component: UserFormComponent },
  { path: 'ai-chat', component: AiChatComponent },
  { path: '**', redirectTo: '' },
];
