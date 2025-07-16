import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SignUpRequest } from '../../models/user.model';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="register-container">
      <div class="register-form">
        <h2>Register for TV Shows</h2>
        <form #registerForm="ngForm" (ngSubmit)="onSubmit(registerForm)">
          <div class="form-row">
            <div class="form-group">
              <label for="firstName">First Name</label>
              <input 
                type="text" 
                id="firstName" 
                name="firstName" 
                [(ngModel)]="userData.firstName"
                required
                #firstName="ngModel">
              <div *ngIf="firstName.invalid && firstName.touched" class="error">
                First name is required
              </div>
            </div>
            
            <div class="form-group">
              <label for="lastName">Last Name</label>
              <input 
                type="text" 
                id="lastName" 
                name="lastName" 
                [(ngModel)]="userData.lastName"
                required
                #lastName="ngModel">
              <div *ngIf="lastName.invalid && lastName.touched" class="error">
                Last name is required
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label for="username">Username</label>
            <input 
              type="text" 
              id="username" 
              name="username" 
              [(ngModel)]="userData.username"
              required
              #username="ngModel">
            <div *ngIf="username.invalid && username.touched" class="error">
              Username is required
            </div>
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input 
              type="email" 
              id="email" 
              name="email" 
              [(ngModel)]="userData.email"
              required
              email
              #email="ngModel">
            <div *ngIf="email.invalid && email.touched" class="error">
              Valid email is required
            </div>
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input 
              type="password" 
              id="password" 
              name="password" 
              [(ngModel)]="userData.password"
              required
              minlength="6"
              #password="ngModel">
            <div *ngIf="password.invalid && password.touched" class="error">
              Password must be at least 6 characters
            </div>
          </div>
          
          <div class="membership-info">
            <h3>Your Membership</h3>
            <div class="free-membership">
              <span class="membership-badge">FREE</span>
              <p>Start with a free account and upgrade to Premium anytime!</p>
              <ul>
                <li>Browse all TV shows</li>
                <li>Add up to 10 shows to watchlist</li>
                <li>Write reviews and ratings</li>
              </ul>
            </div>
          </div>
          
          <button type="submit" [disabled]="registerForm.invalid || loading">
            {{ loading ? 'Creating Account...' : 'Create Free Account' }}
          </button>
          
          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>
          
          <div *ngIf="successMessage" class="success-message">
            {{ successMessage }}
          </div>
        </form>
        
        <p class="login-link">
          Already have an account? <a routerLink="/login">Login here</a>
        </p>
      </div>
    </div>
  `,
    styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem 0;
    }
    
    .register-form {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      width: 100%;
      max-width: 500px;
    }
    
    h2 {
      text-align: center;
      margin-bottom: 1.5rem;
      color: #333;
    }
    
    .form-row {
      display: flex;
      gap: 1rem;
    }
    
    .form-group {
      margin-bottom: 1rem;
      flex: 1;
    }
    
    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #555;
    }
    
    input {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1rem;
    }
    
    input:focus {
      outline: none;
      border-color: #667eea;
    }
    
    .membership-info {
      background: #f8f9fa;
      padding: 1rem;
      border-radius: 6px;
      margin-bottom: 1.5rem;
    }
    
    .membership-info h3 {
      margin: 0 0 1rem 0;
      color: #333;
      font-size: 1.1rem;
    }
    
    .free-membership {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    
    .membership-badge {
      background: #28a745;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.875rem;
      font-weight: bold;
      align-self: flex-start;
    }
    
    .membership-info p {
      margin: 0;
      color: #666;
      font-size: 0.9rem;
    }
    
    .membership-info ul {
      margin: 0.5rem 0 0 1rem;
      padding: 0;
    }
    
    .membership-info li {
      color: #666;
      font-size: 0.875rem;
      margin-bottom: 0.25rem;
    }
    
    button {
      width: 100%;
      padding: 0.75rem;
      background: #667eea;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 1rem;
      cursor: pointer;
    }
    
    button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    
    .error {
      color: #e74c3c;
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }
    
    .error-message {
      color: #e74c3c;
      text-align: center;
      margin-top: 1rem;
    }
    
    .success-message {
      color: #27ae60;
      text-align: center;
      margin-top: 1rem;
    }
    
    .login-link {
      text-align: center;
      margin-top: 1rem;
    }
    
    .login-link a {
      color: #667eea;
      text-decoration: none;
    }
  `]
})
export class RegisterComponent {
    userData: SignUpRequest = {
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        password: '',
        membership: 'FREE',
        role: 'USER'
    };
    loading = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    onSubmit(form: any): void {
        if (form.valid) {
            this.loading = true;
            this.errorMessage = '';
            this.successMessage = '';

            this.authService.register(this.userData).subscribe({
                next: (user) => {
                    this.successMessage = 'Registration successful! You can now login with your free account.';
                    setTimeout(() => {
                        this.router.navigate(['/login']);
                    }, 2000);
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Registration failed';
                    this.loading = false;
                },
                complete: () => {
                    this.loading = false;
                }
            });
        }
    }
}