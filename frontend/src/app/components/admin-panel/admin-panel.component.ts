import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { Review } from '../../models/review.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-panel" *ngIf="isAdmin">
      <div class="panel-header">
        <h1>Admin Panel</h1>
        <p>Review moderation and system management</p>
      </div>

      <div class="admin-tabs">
        <button 
          class="tab-btn"
          [class.active]="activeTab === 'reviews'"
          (click)="setActiveTab('reviews')">
          Pending Reviews
          <span class="badge" *ngIf="pendingReviews.length > 0">{{ pendingReviews.length }}</span>
        </button>
        <button 
          class="tab-btn"
          [class.active]="activeTab === 'users'"
          (click)="setActiveTab('users')">
          User Management
        </button>
      </div>

      <div class="panel-content">
        <!-- Pending Reviews Section -->
        <div class="section" *ngIf="activeTab === 'reviews'">
          <div class="section-header">
            <h2>Pending Reviews</h2>
            <span class="badge" *ngIf="pendingReviews.length > 0">{{ pendingReviews.length }}</span>
          </div>
          
          <div class="reviews-list" *ngIf="pendingReviews.length > 0">
            <div class="review-card pending" *ngFor="let review of pendingReviews">
              <div class="review-header">
                <div class="reviewer-info">
                  <span class="reviewer-name">{{ review.username }}</span>
                  <span class="review-date">{{ review.createdAt | date:'medium' }}</span>
                </div>
                <div class="review-rating">
                  <span *ngFor="let star of getStars(review.rating)" class="star">⭐</span>
                </div>
              </div>
              
              <div class="show-info">
                <strong>TV Show ID:</strong> {{ review.tvShowId }}
              </div>
              
              <div class="review-content">
                {{ review.content }}
              </div>
              
              <div class="review-actions">
                <button 
                  (click)="approveReview(review.id)" 
                  class="btn btn-approve"
                  [disabled]="processingReviews.has(review.id)">
                  {{ processingReviews.has(review.id) ? 'Approving...' : 'Approve' }}
                </button>
                <button 
                  (click)="rejectReview(review.id)" 
                  class="btn btn-reject"
                  [disabled]="processingReviews.has(review.id)">
                  {{ processingReviews.has(review.id) ? 'Rejecting...' : 'Reject' }}
                </button>
              </div>
            </div>
          </div>
          
          <div class="empty-state" *ngIf="pendingReviews.length === 0">
            <div class="empty-icon">✅</div>
            <h3>All caught up!</h3>
            <p>No reviews pending approval.</p>
          </div>
        </div>

        <!-- Statistics Section -->
        <div class="section" *ngIf="activeTab === 'reviews'">
          <h2>Review Statistics</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-value">{{ totalReviews }}</div>
              <div class="stat-label">Total Reviews</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ pendingReviews.length }}</div>
              <div class="stat-label">Pending Approval</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ approvedReviews }}</div>
              <div class="stat-label">Approved Reviews</div>
            </div>
          </div>
        </div>

        <!-- User Management Section -->
        <div class="section" *ngIf="activeTab === 'users'">
          <div class="section-header">
            <h2>User Management</h2>
            <span class="badge">{{ allUsers.length }} users</span>
          </div>
          
          <div class="users-list" *ngIf="allUsers.length > 0">
            <div class="user-card" *ngFor="let user of allUsers">
              <div class="user-info">
                <div class="user-details">
                  <span class="username">{{ user.username }}</span>
                  <span class="email">{{ user.email }}</span>
                </div>
                <div class="user-role">
                  <span class="role-badge" [class]="'role-' + user.role.toLowerCase()">
                    {{ user.role }}
                  </span>
                </div>
              </div>
              
              <div class="user-actions" *ngIf="user.role !== 'ADMIN'">
                <button 
                  (click)="promoteToAdmin(user.username)" 
                  class="btn btn-promote"
                  [disabled]="promotingUsers.has(user.username)">
                  {{ promotingUsers.has(user.username) ? 'Promoting...' : 'Promote to Admin' }}
                </button>
              </div>
            </div>
          </div>
          
          <div class="empty-state" *ngIf="allUsers.length === 0">
            <div class="empty-icon">👥</div>
            <h3>No users found</h3>
            <p>No users are registered in the system.</p>
          </div>
        </div>
      </div>
    </div>

    <div class="unauthorized" *ngIf="!isAdmin">
      <div class="error-content">
        <div class="error-icon">🚫</div>
        <h2>Access Denied</h2>
        <p>You don't have permission to access this page.</p>
        <button (click)="goBack()" class="btn btn-primary">Go Back</button>
      </div>
    </div>
  `,
  styles: [`
    .admin-panel {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .panel-header {
      text-align: center;
      margin-bottom: 3rem;
      padding: 2rem;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 12px;
    }

    .panel-header h1 {
      margin: 0 0 0.5rem 0;
      font-size: 2.5rem;
      font-weight: bold;
    }

    .panel-header p {
      margin: 0;
      opacity: 0.9;
      font-size: 1.125rem;
    }

    .panel-content {
      display: flex;
      flex-direction: column;
      gap: 2rem;
    }

    .section {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .section-header h2 {
      margin: 0;
      color: #1f2937;
      font-size: 1.5rem;
    }

    .badge {
      background: #ef4444;
      color: white;
      font-size: 0.75rem;
      font-weight: bold;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      min-width: 20px;
      text-align: center;
    }

    .reviews-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .review-card {
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1.5rem;
      background: #f9fafb;
    }

    .review-card.pending {
      border-color: #fbbf24;
      background: #fef3c7;
    }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
    }

    .reviewer-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .reviewer-name {
      font-weight: 600;
      color: #1f2937;
      font-size: 1rem;
    }

    .review-date {
      font-size: 0.75rem;
      color: #6b7280;
    }

    .review-rating .star {
      font-size: 0.875rem;
    }

    .show-info {
      margin-bottom: 1rem;
      font-size: 0.875rem;
      color: #6b7280;
    }

    .review-content {
      margin-bottom: 1.5rem;
      line-height: 1.5;
      color: #374151;
      background: white;
      padding: 1rem;
      border-radius: 6px;
      border: 1px solid #e5e7eb;
    }

    .review-actions {
      display: flex;
      gap: 1rem;
    }

    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
      font-size: 0.875rem;
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-approve {
      background: #10b981;
      color: white;
    }

    .btn-approve:hover:not(:disabled) {
      background: #059669;
    }

    .btn-reject {
      background: #ef4444;
      color: white;
    }

    .btn-reject:hover:not(:disabled) {
      background: #dc2626;
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      color: #6b7280;
    }

    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    .empty-state h3 {
      margin: 0 0 0.5rem 0;
      color: #1f2937;
      font-size: 1.25rem;
    }

    .empty-state p {
      margin: 0;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
    }

    .stat-card {
      background: #f8fafc;
      padding: 1.5rem;
      border-radius: 8px;
      text-align: center;
      border: 1px solid #e2e8f0;
    }

    .stat-value {
      font-size: 2rem;
      font-weight: bold;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }

    .stat-label {
      font-size: 0.875rem;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .unauthorized {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 70vh;
    }

    .error-content {
      text-align: center;
      max-width: 400px;
    }

    .error-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }

    .error-content h2 {
      margin: 0 0 1rem 0;
      color: #1f2937;
      font-size: 1.5rem;
    }

    .error-content p {
      margin: 0 0 2rem 0;
      color: #6b7280;
    }

    .btn-primary {
      background: #4f46e5;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: #4338ca;
    }

    @media (max-width: 768px) {
      .admin-panel {
        padding: 1rem;
      }
      
      .panel-header {
        padding: 1.5rem;
      }
      
      .panel-header h1 {
        font-size: 2rem;
      }
      
      .review-header {
        flex-direction: column;
        gap: 1rem;
      }
      
      .review-actions {
        flex-direction: column;
      }
      
      .btn {
        width: 100%;
      }
    }

    /* Tabs Styles */
    .admin-tabs {
      display: flex;
      border-bottom: 2px solid #e5e7eb;
      margin-bottom: 2rem;
    }

    .tab-btn {
      background: none;
      border: none;
      padding: 1rem 2rem;
      font-size: 1rem;
      font-weight: 500;
      color: #6b7280;
      cursor: pointer;
      border-bottom: 3px solid transparent;
      transition: all 0.2s ease;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .tab-btn:hover {
      color: #374151;
      background: #f9fafb;
    }

    .tab-btn.active {
      color: #2563eb;
      border-bottom-color: #2563eb;
      background: #f8fafc;
    }

    /* User Management Styles */
    .users-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .user-card {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 2rem;
    }

    .user-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .username {
      font-weight: 600;
      color: #111827;
      font-size: 1rem;
    }

    .email {
      color: #6b7280;
      font-size: 0.875rem;
    }

    .role-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .role-free {
      background: #f3f4f6;
      color: #374151;
    }

    .role-premium {
      background: #dbeafe;
      color: #2563eb;
    }

    .role-admin {
      background: #fef3c7;
      color: #d97706;
    }

    .btn-promote {
      background: #f59e0b;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      font-size: 0.875rem;
      font-weight: 500;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .btn-promote:hover:not(:disabled) {
      background: #d97706;
    }

    .btn-promote:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    }
  `]
})
export class AdminPanelComponent implements OnInit {
  pendingReviews: Review[] = [];
  processingReviews = new Set<string>();
  allUsers: User[] = [];
  promotingUsers = new Set<string>();
  activeTab = 'reviews';
  currentUser: any;
  
  // Mock statistics - you could implement real endpoints for these
  totalReviews = 0;
  approvedReviews = 0;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    if (this.isAdmin) {
      this.loadPendingReviews();
      this.loadAllUsers();
    }
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  loadPendingReviews(): void {
    this.reviewService.getPendingReviews().subscribe({
      next: (reviews) => {
        this.pendingReviews = reviews;
        this.updateStatistics();
      },
      error: (error) => {
        console.error('Error loading pending reviews:', error);
      }
    });
  }

  approveReview(reviewId: string): void {
    this.processingReviews.add(reviewId);
    
    this.reviewService.approveReview(reviewId).subscribe({
      next: () => {
        this.pendingReviews = this.pendingReviews.filter(r => r.id !== reviewId);
        this.updateStatistics();
      },
      error: (error) => {
        console.error('Error approving review:', error);
        alert('Error approving review: ' + (error.error?.message || 'Unknown error'));
      },
      complete: () => {
        this.processingReviews.delete(reviewId);
      }
    });
  }

  rejectReview(reviewId: string): void {
    if (!confirm('Are you sure you want to reject this review? This action cannot be undone.')) {
      return;
    }

    this.processingReviews.add(reviewId);
    
    this.reviewService.rejectReview(reviewId).subscribe({
      next: () => {
        this.pendingReviews = this.pendingReviews.filter(r => r.id !== reviewId);
        this.updateStatistics();
      },
      error: (error) => {
        console.error('Error rejecting review:', error);
        alert('Error rejecting review: ' + (error.error?.message || 'Unknown error'));
      },
      complete: () => {
        this.processingReviews.delete(reviewId);
      }
    });
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  private updateStatistics(): void {
    // Simple statistics based on current data
    // In a real application, you would have dedicated endpoints for comprehensive stats
    this.totalReviews = this.pendingReviews.length; // This would include all reviews
    this.approvedReviews = 0; // This would come from an endpoint
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    if (tab === 'users' && this.allUsers.length === 0) {
      this.loadAllUsers();
    }
  }

  loadAllUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        alert('Error loading users: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  promoteToAdmin(username: string): void {
    if (!confirm(`Promote ${username} to admin? This action cannot be undone.`)) {
      return;
    }

    this.promotingUsers.add(username);
    
    this.authService.promoteToAdmin(username).subscribe({
      next: (updatedUser) => {
        // Update the user in the list
        const userIndex = this.allUsers.findIndex(u => u.username === username);
        if (userIndex !== -1) {
          this.allUsers[userIndex] = updatedUser;
        }
        alert(`${username} has been promoted to admin successfully!`);
      },
      error: (error) => {
        console.error('Error promoting user:', error);
        alert('Error promoting user: ' + (error.error?.message || 'Unknown error'));
      },
      complete: () => {
        this.promotingUsers.delete(username);
      }
    });
  }
}