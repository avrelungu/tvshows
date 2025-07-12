import { Component } from '@angular/core';
import {Router, RouterLink} from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/user.model';
import {FormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
    selector: 'app-login',
    template: `
        <div class="login-container">
            <div class="login-form">
                <h2>Login to TV Shows</h2>
                <form #loginForm="ngForm" (ngSubmit)="onSubmit(loginForm)">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input
                                type="text"
                                id="username"
                                name="username"
                                [(ngModel)]="credentials.username"
                                required
                                #username="ngModel">
                        <div *ngIf="username.invalid && username.touched" class="error">
                            Username is required
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <input
                                type="password"
                                id="password"
                                name="password"
                                [(ngModel)]="credentials.password"
                                required
                                #password="ngModel">
                        <div *ngIf="password.invalid && password.touched" class="error">
                            Password is required
                        </div>
                    </div>

                    <button type="submit" [disabled]="loginForm.invalid || loading">
                        {{ loading ? 'Logging in...' : 'Login' }}
                    </button>

                    <div *ngIf="errorMessage" class="error-message">
                        {{ errorMessage }}
                    </div>
                </form>

                <p class="register-link">
                    Don't have an account? <a routerLink="/register">Register here</a>
                </p>
            </div>
        </div>
    `,
    imports: [
        RouterLink,
        FormsModule,
        NgIf
    ],
    styles: [`
        .login-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .login-form {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 400px;
        }

        h2 {
            text-align: center;
            margin-bottom: 1.5rem;
            color: #333;
        }

        .form-group {
            margin-bottom: 1rem;
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

        .register-link {
            text-align: center;
            margin-top: 1rem;
        }

        .register-link a {
            color: #667eea;
            text-decoration: none;
        }
    `]
})
export class LoginComponent {
    credentials: LoginRequest = { username: '', password: '' };
    loading = false;
    errorMessage = '';

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    onSubmit(form: any): void {
        if (form.valid) {
            this.loading = true;
            this.errorMessage = '';

            this.authService.login(this.credentials).subscribe({
                next: (user) => {
                    this.router.navigate(['/dashboard']);
                },
                error: (error) => {
                    this.errorMessage = error.error?.message || 'Login failed';
                    this.loading = false;
                },
                complete: () => {
                    this.loading = false;
                }
            });
        }
    }
}