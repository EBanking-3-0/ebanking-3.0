import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { User, UserService } from '../../services/user.service';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.scss'],
})
export class UserFormComponent implements OnInit {
  user: Partial<User> = {
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
  };
  isEditMode = false;
  userId: string | null = null;
  loading = false;
  error: any;

  constructor(
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    if (this.userId) {
      this.isEditMode = true;
      this.loadUser(this.userId);
    }
  }

  loadUser(id: string): void {
    this.loading = true;
    this.error = null;
    this.userService.getUser(id).subscribe({
      next: (user) => {
        if (user) {
          this.user = { ...user };
          console.log('Loaded user for edit:', user);
        }
        this.loading = false;
      },
      error: (error) => {
        this.error = error;
        this.loading = false;
        console.error('Error loading user:', error);
        alert('Failed to load user data');
      },
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.error = null;

    if (this.isEditMode && this.userId) {
      const { firstName, lastName, phone } = this.user;
      this.userService
        .updateUser(this.userId, { firstName: firstName!, lastName: lastName!, phone: phone! })
        .subscribe({
          next: (updatedUser) => {
            console.log('User updated:', updatedUser);
            this.loading = false;
            this.router.navigate(['/']);
          },
          error: (error) => {
            this.error = error;
            this.loading = false;
            console.error('Error updating user:', error);
            alert('Failed to update user: ' + (error.message || 'Unknown error'));
          },
        });
    } else {
      const { email, firstName, lastName, phone } = this.user;
      this.userService
        .createUser({ email: email!, firstName: firstName!, lastName: lastName!, phone: phone! })
        .subscribe({
          next: (newUser) => {
            console.log('User created:', newUser);
            this.loading = false;
            this.router.navigate(['/']);
          },
          error: (error) => {
            this.error = error;
            this.loading = false;
            console.error('Error creating user:', error);
            alert('Failed to create user: ' + (error.message || 'Unknown error'));
          },
        });
    }
  }

  cancel(): void {
    this.router.navigate(['/']);
  }
}
