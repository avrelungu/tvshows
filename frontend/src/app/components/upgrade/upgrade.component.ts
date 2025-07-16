import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UpgradeService } from '../../services/upgrade.service';
import { UserProfile, UpgradeProfileRequest } from '../../models/user.model';

@Component({
    selector: 'app-upgrade',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="upgrade-container">
      <div class="upgrade-content">
        <div class="current-plan" *ngIf="currentUser">
          <h2>Current Plan</h2>
          <div class="plan-card current">
            <span class="plan-badge free">{{ currentUser.role }}</span>
            <h3>Free Membership</h3>
            <ul>
              <li>Browse all TV shows</li>
              <li>Add up to 10 shows to watchlist</li>
              <li>Write reviews and ratings</li>
            </ul>
          </div>
        </div>

        <div class="upgrade-plan">
          <h2>Upgrade to Premium</h2>
          <div class="plan-card premium">
            <span class="plan-badge premium">PREMIUM</span>
            <h3>Premium Membership</h3>
            <div class="price">$9.99<span>/month</span></div>
            <ul>
              <li>✓ Everything in Free</li>
              <li>✓ Unlimited watchlist</li>
              <li>✓ Priority support</li>
              <li>✓ Advanced search filters</li>
              <li>✓ Export watchlist</li>
              <li>✓ Ad-free experience</li>
            </ul>
            
            <div class="upgrade-form" *ngIf="!isUpgrading">
              <h4>Ready to upgrade?</h4>
              <p>Your account will be upgraded immediately after confirmation.</p>
              
              <div class="form-group">
                <label>
                  <input 
                    type="checkbox" 
                    [(ngModel)]="agreedToTerms"
                    required>
                  I agree to the <a href="#" target="_blank">Terms of Service</a> and <a href="#" target="_blank">Privacy Policy</a>
                </label>
              </div>
              
              <button 
                class="upgrade-btn"
                [disabled]="!agreedToTerms || loading"
                (click)="upgradeAccount()">
                {{ loading ? 'Processing...' : 'Upgrade to Premium - $9.99/month' }}
              </button>
              
              <p class="upgrade-note">
                You can cancel anytime. No long-term commitments.
              </p>
            </div>
            
            <div class="upgrade-success" *ngIf="isUpgrading">
              <div class="success-icon">✓</div>
              <h4>Welcome to Premium!</h4>
              <p>Your account has been successfully upgraded to Premium membership.</p>
              <button (click)="goToDashboard()" class="dashboard-btn">
                Go to Dashboard
              </button>
            </div>
          </div>
        </div>
        
        <div *ngIf="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
        
        <div class="back-link">
          <a routerLink="/dashboard">← Back to Dashboard</a>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .upgrade-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    
    .upgrade-content {
      max-width: 900px;
      width: 100%;
    }
    
    .current-plan, .upgrade-plan {
      margin-bottom: 2rem;
    }
    
    .current-plan h2, .upgrade-plan h2 {
      color: white;
      text-align: center;
      margin-bottom: 1rem;
    }
    
    .plan-card {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      position: relative;
    }
    
    .plan-card.premium {
      border: 3px solid #f39c12;
      transform: scale(1.02);
    }
    
    .plan-badge {
      position: absolute;
      top: -10px;
      left: 2rem;
      padding: 0.5rem 1rem;
      border-radius: 20px;
      font-weight: bold;
      font-size: 0.875rem;
    }
    
    .plan-badge.free {
      background: #95a5a6;
      color: white;
    }
    
    .plan-badge.premium {
      background: #f39c12;
      color: white;
    }
    
    .plan-card h3 {
      margin: 1rem 0;
      color: #2c3e50;
    }
    
    .price {
      font-size: 2.5rem;
      font-weight: bold;
      color: #2c3e50;
      margin: 1rem 0;
    }
    
    .price span {
      font-size: 1rem;
      color: #7f8c8d;
    }
    
    .plan-card ul {
      list-style: none;
      padding: 0;
      margin: 1.5rem 0;
    }
    
    .plan-card li {
      padding: 0.5rem 0;
      color: #34495e;
      border-bottom: 1px solid #ecf0f1;
    }
    
    .plan-card li:last-child {
      border-bottom: none;
    }
    
    .upgrade-form {
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 2px solid #ecf0f1;
    }
    
    .upgrade-form h4 {
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }
    
    .upgrade-form p {
      color: #7f8c8d;
      margin-bottom: 1.5rem;
    }
    
    .form-group {
      margin-bottom: 1.5rem;
    }
    
    .form-group label {
      display: flex;
      align-items: flex-start;
      gap: 0.5rem;
      font-size: 0.9rem;
      color: #34495e;
      line-height: 1.4;
    }
    
    .form-group input[type="checkbox"] {
      margin-top: 0.2rem;
    }
    
    .form-group a {
      color: #3498db;
      text-decoration: none;
    }
    
    .upgrade-btn {
      width: 100%;
      padding: 1rem;
      background: #f39c12;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 1.1rem;
      font-weight: bold;
      cursor: pointer;
      transition: background-color 0.3s ease;
    }
    
    .upgrade-btn:hover:not(:disabled) {
      background: #e67e22;
    }
    
    .upgrade-btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    
    .upgrade-note {
      text-align: center;
      font-size: 0.875rem;
      color: #7f8c8d;
      margin-top: 1rem;
    }
    
    .upgrade-success {
      text-align: center;
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 2px solid #ecf0f1;
    }
    
    .success-icon {
      font-size: 3rem;
      color: #27ae60;
      margin-bottom: 1rem;
    }
    
    .upgrade-success h4 {
      color: #27ae60;
      margin-bottom: 0.5rem;
    }
    
    .upgrade-success p {
      color: #7f8c8d;
      margin-bottom: 1.5rem;
    }
    
    .dashboard-btn {
      padding: 0.75rem 2rem;
      background: #3498db;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 1rem;
    }
    
    .dashboard-btn:hover {
      background: #2980b9;
    }
    
    .error-message {
      background: #e74c3c;
      color: white;
      padding: 1rem;
      border-radius: 6px;
      text-align: center;
      margin-top: 1rem;
    }
    
    .back-link {
      text-align: center;
      margin-top: 2rem;
    }
    
    .back-link a {
      color: white;
      text-decoration: none;
      font-size: 1.1rem;
    }
    
    .back-link a:hover {
      text-decoration: underline;
    }
    
    @media (min-width: 768px) {
      .upgrade-content {
        display: grid;
        grid-template-columns: 1fr 1.5fr;
        gap: 2rem;
        align-items: start;
      }
    }
  `]
})
export class UpgradeComponent implements OnInit {
    currentUser: any;
    loading = false;
    errorMessage = '';
    agreedToTerms = false;
    isUpgrading = false;

    constructor(
        private userService: UserService,
        private authService: AuthService,
        private upgradeService: UpgradeService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();

        // Redirect if already premium
        if (this.currentUser?.membership === 'PREMIUM') {
            this.router.navigate(['/dashboard']);
        }
    }

    upgradeAccount(): void {
        if (!this.agreedToTerms || !this.currentUser) return;

        this.loading = true;
        this.errorMessage = '';

        this.upgradeService.upgradeUserToPremium(this.currentUser.username).subscribe({
            next: (profile) => {
                // Refresh the user token to get updated membership info
                this.authService.refreshUserToken().subscribe({
                    next: (updatedUser) => {
                        console.log('Token refreshed with new membership:', updatedUser);
                        this.isUpgrading = true;
                    },
                    error: (error) => {
                        console.error('Error refreshing token:', error);
                        // Still show success since upgrade worked
                        this.isUpgrading = true;
                    }
                });
            },
            error: (error) => {
                this.errorMessage = error.error?.message || 'Upgrade failed. Please try again.';
                this.loading = false;
            },
            complete: () => {
                this.loading = false;
            }
        });
    }

    goToDashboard(): void {
        this.router.navigate(['/dashboard']);
    }
}