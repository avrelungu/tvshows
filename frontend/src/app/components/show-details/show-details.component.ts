import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TvShowService } from '../../services/tvshow.service';
import { AuthService } from '../../services/auth.service';
import { ReviewService } from '../../services/review.service';
import { ReviewModalComponent } from '../review-modal/review-modal.component';
import { TvShow } from '../../models/tvshow.model';
import { Review, ReviewStats } from '../../models/review.model';

@Component({
  selector: 'app-show-details',
  standalone: true,
  imports: [CommonModule, ReviewModalComponent],
  template: `
    <div class="show-details" *ngIf="show">
      <!-- Header Section -->
      <div class="show-header">
        <img [src]="show.imageOriginal || show.imageMedium" [alt]="show.name" class="show-poster">
        <div class="show-info">
          <h1>{{ show.name }}</h1>
          <div class="show-meta">
            <span class="rating">⭐ {{ show.rating }}/10</span>
            <span class="status" [class]="show.status.toLowerCase()">{{ show.status }}</span>
            <span class="language">{{ show.language }}</span>
            <span class="runtime" *ngIf="show.runtime">{{ show.runtime }} min</span>
          </div>
          <div class="genres" *ngIf="show.genres?.length">
            <span class="genre-tag" *ngFor="let genre of show.genres">{{ genre }}</span>
          </div>
          <div class="show-dates">
            <span *ngIf="show.premiered">Premiered: {{ show.premiered | date:'mediumDate' }}</span>
            <span *ngIf="show.ended">Ended: {{ show.ended | date:'mediumDate' }}</span>
          </div>
          <div class="action-buttons">
            <button (click)="addToWatchlist()" [disabled]="addingToWatchlist" class="watchlist-btn">
              Add to Watchlist
            </button>
            <button 
              (click)="openReviewModal()" 
              class="review-btn" 
              *ngIf="canWriteReview">
              {{ userReview ? 'Edit Review' : 'Write Review' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Synopsis Section -->
      <div class="content-section" *ngIf="show.summary">
        <h2>Synopsis</h2>
        <div class="synopsis" [innerHTML]="show.summary"></div>
      </div>

      <!-- Show Details -->
      <div class="content-section">
        <h2>Show Details</h2>
        <div class="details-grid">
          <div class="detail-item" *ngIf="show.scheduleTime || show.scheduleDays?.length">
            <strong>Schedule:</strong>
            <span>{{ formatSchedule() }}</span>
          </div>
          <div class="detail-item" *ngIf="show.averageRuntime">
            <strong>Average Runtime:</strong>
            <span>{{ show.averageRuntime }} minutes</span>
          </div>
          <div class="detail-item" *ngIf="show.officialSite">
            <strong>Official Site:</strong>
            <a [href]="show.officialSite" target="_blank" rel="noopener">Visit Website</a>
          </div>
          <div class="detail-item" *ngIf="show.imdb">
            <strong>IMDb:</strong>
            <a [href]="'https://www.imdb.com/title/' + show.imdb" target="_blank" rel="noopener">
              View on IMDb
            </a>
          </div>
        </div>
      </div>

      <!-- Community Reviews Section -->
      <div class="content-section">
        <div class="reviews-header">
          <h2>Our Community Rating</h2>
          <div class="community-stats" *ngIf="reviewStats">
            <div class="stats-item">
              <span class="stats-value">{{ reviewStats.averageRating | number:'1.1-1' }}/5</span>
              <span class="stats-label">Average Rating</span>
            </div>
            <div class="stats-item">
              <span class="stats-value">{{ reviewStats.totalReviews }}</span>
              <span class="stats-label">Reviews</span>
            </div>
          </div>
        </div>

        <!-- User's Review (if exists) -->
        <div class="user-review-section" *ngIf="userReview">
          <h3>Your Review</h3>
          <div class="review-card user-review">
            <div class="review-header">
              <div class="review-rating">
                <span *ngFor="let star of getStars(userReview.rating)" class="star">⭐</span>
              </div>
              <div class="review-actions">
                <button (click)="openReviewModal()" class="edit-btn">Edit</button>
                <button (click)="deleteReview()" class="delete-btn">Delete</button>
              </div>
            </div>
            <p class="review-content">{{ userReview.content }}</p>
            <div class="review-meta">
              <span class="review-date">{{ userReview.createdAt | date:'medium' }}</span>
              <span class="pending-indicator" *ngIf="!userReview.isApproved">Pending Approval</span>
            </div>
          </div>
        </div>

        <!-- Community Reviews -->
        <div class="community-reviews">
          <h3>Community Reviews</h3>
          <div class="reviews-list" *ngIf="communityReviews.length > 0">
            <div 
              class="review-card" 
              *ngFor="let review of communityReviews"
              [class.pending]="!review.isApproved">
              <div class="review-header">
                <span class="reviewer-name">{{ review.username }}</span>
                <div class="review-rating">
                  <span *ngFor="let star of getStars(review.rating)" class="star">⭐</span>
                </div>
              </div>
              <p class="review-content">{{ review.content }}</p>
              <div class="review-meta">
                <span class="review-date">{{ review.createdAt | date:'medium' }}</span>
                <span class="pending-indicator" *ngIf="!review.isApproved">Pending Approval</span>
              </div>
            </div>
          </div>
          <div class="no-reviews" *ngIf="communityReviews.length === 0">
            <p>No community reviews yet. Be the first to share your thoughts!</p>
          </div>
        </div>

        <!-- Premium Upgrade Notice -->
        <div class="upgrade-notice" *ngIf="!canWriteReview">
          <h3>Want to share your thoughts?</h3>
          <p>Upgrade to Premium to write reviews and join the community discussion!</p>
          <button (click)="goToUpgrade()" class="upgrade-btn">Upgrade to Premium</button>
        </div>
      </div>
    </div>

    <div class="loading" *ngIf="!show">
      <p>Loading show details...</p>
    </div>

    <!-- Review Modal -->
    <app-review-modal
      [isOpen]="isReviewModalOpen"
      [tvShowId]="show?.id || null"
      [showName]="show?.name || ''"
      [existingReview]="userReview"
      (close)="closeReviewModal()"
      (reviewSubmitted)="onReviewSubmitted()">
    </app-review-modal>
  `,
  styles: [`
    .show-details {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem;
    }

    .show-header {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 2rem;
      margin-bottom: 3rem;
      background: white;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .show-poster {
      width: 100%;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }

    .show-info h1 {
      margin: 0 0 1rem 0;
      color: #1f2937;
      font-size: 2rem;
    }

    .show-meta {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }

    .show-meta span {
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .rating {
      background: #fbbf24;
      color: #92400e;
    }

    .status {
      background: #e5e7eb;
      color: #374151;
    }

    .status.running {
      background: #d1fae5;
      color: #065f46;
    }

    .status.ended {
      background: #fee2e2;
      color: #991b1b;
    }

    .language, .runtime {
      background: #e0e7ff;
      color: #3730a3;
    }

    .genres {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }

    .genre-tag {
      padding: 0.25rem 0.75rem;
      background: #f3f4f6;
      border: 1px solid #d1d5db;
      border-radius: 20px;
      font-size: 0.75rem;
      color: #374151;
    }

    .show-dates {
      margin-bottom: 1.5rem;
      color: #6b7280;
      font-size: 0.875rem;
    }

    .show-dates span {
      display: block;
      margin-bottom: 0.25rem;
    }

    .action-buttons {
      display: flex;
      gap: 1rem;
    }

    .watchlist-btn, .review-btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .watchlist-btn {
      background: #4f46e5;
      color: white;
    }

    .watchlist-btn:hover {
      background: #4338ca;
    }

    .review-btn {
      background: #10b981;
      color: white;
    }

    .review-btn:hover {
      background: #059669;
    }

    .content-section {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      margin-bottom: 2rem;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .content-section h2 {
      margin: 0 0 1rem 0;
      color: #1f2937;
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 0.5rem;
    }

    .synopsis {
      line-height: 1.6;
      color: #374151;
    }

    .details-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
    }

    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .detail-item strong {
      color: #1f2937;
      font-size: 0.875rem;
    }

    .detail-item a {
      color: #4f46e5;
      text-decoration: none;
    }

    .detail-item a:hover {
      text-decoration: underline;
    }

    .reviews-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .community-stats {
      display: flex;
      gap: 2rem;
    }

    .stats-item {
      text-align: center;
    }

    .stats-value {
      display: block;
      font-size: 1.5rem;
      font-weight: bold;
      color: #1f2937;
    }

    .stats-label {
      font-size: 0.75rem;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .user-review-section {
      margin-bottom: 2rem;
      padding-bottom: 2rem;
      border-bottom: 1px solid #e5e7eb;
    }

    .user-review-section h3 {
      margin: 0 0 1rem 0;
      color: #1f2937;
    }

    .review-card {
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 1.5rem;
      margin-bottom: 1rem;
    }

    .review-card.user-review {
      background: #eff6ff;
      border-color: #bfdbfe;
    }

    .review-card.pending {
      background: #fef3c7;
      border-color: #fbbf24;
    }

    .review-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }

    .reviewer-name {
      font-weight: 600;
      color: #1f2937;
    }

    .review-rating .star {
      font-size: 0.875rem;
    }

    .review-actions {
      display: flex;
      gap: 0.5rem;
    }

    .edit-btn, .delete-btn {
      padding: 0.25rem 0.75rem;
      border: none;
      border-radius: 4px;
      font-size: 0.75rem;
      cursor: pointer;
    }

    .edit-btn {
      background: #3b82f6;
      color: white;
    }

    .delete-btn {
      background: #ef4444;
      color: white;
    }

    .review-content {
      margin: 0 0 1rem 0;
      line-height: 1.5;
      color: #374151;
    }

    .review-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 0.75rem;
      color: #6b7280;
    }

    .pending-indicator {
      background: #fbbf24;
      color: #92400e;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-weight: 500;
    }

    .no-reviews {
      text-align: center;
      padding: 3rem 0;
      color: #6b7280;
    }

    .upgrade-notice {
      background: linear-gradient(135deg, #f59e0b, #d97706);
      color: white;
      padding: 2rem;
      border-radius: 8px;
      text-align: center;
      margin-top: 2rem;
    }

    .upgrade-notice h3 {
      margin: 0 0 0.5rem 0;
    }

    .upgrade-notice p {
      margin: 0 0 1rem 0;
      opacity: 0.9;
    }

    .upgrade-btn {
      background: white;
      color: #f59e0b;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 6px;
      font-weight: bold;
      cursor: pointer;
    }

    .loading {
      text-align: center;
      padding: 4rem 0;
      color: #6b7280;
    }

    @media (max-width: 768px) {
      .show-header {
        grid-template-columns: 1fr;
        text-align: center;
      }
      
      .reviews-header {
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;
      }
      
      .community-stats {
        align-self: stretch;
        justify-content: space-around;
      }
    }
  `]
})
export class ShowDetailsComponent implements OnInit {
  show: TvShow | null = null;
  communityReviews: Review[] = [];
  userReview: Review | null = null;
  reviewStats: ReviewStats | null = null;
  addingToWatchlist = false;
  isReviewModalOpen = false;
  currentUser: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tvShowService: TvShowService,
    private authService: AuthService,
    private reviewService: ReviewService
  ) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    const showId = this.route.snapshot.paramMap.get('id');
    if (showId) {
      this.loadShowDetails(+showId);
      this.loadCommunityReviews(+showId);
      this.loadReviewStats(+showId);
      this.loadUserReview(+showId);
    }
  }

  get canWriteReview(): boolean {
    return this.currentUser?.role === 'PREMIUM' || this.currentUser?.role === 'ADMIN';
  }

  loadShowDetails(showId: number): void {
    this.tvShowService.getTvShowById(showId).subscribe({
      next: (show) => {
        this.show = show;
      },
      error: (error) => {
        console.error('Error loading show details:', error);
        this.router.navigate(['/dashboard']);
      }
    });
  }

  loadCommunityReviews(showId: number): void {
    this.reviewService.getReviewsByTvShow(showId).subscribe({
      next: (reviews) => {
        this.communityReviews = reviews.content;
      },
      error: (error) => {
        console.error('Error loading community reviews:', error);
      }
    });
  }

  loadReviewStats(showId: number): void {
    this.reviewService.getReviewStats(showId).subscribe({
      next: (stats) => {
        this.reviewStats = stats;
      },
      error: (error) => {
        console.error('Error loading review stats:', error);
      }
    });
  }

  loadUserReview(showId: number): void {
    if (this.currentUser?.username) {
      this.reviewService.getUserReviewForShow(showId, this.currentUser.username).subscribe({
        next: (review) => {
          this.userReview = review;
        },
        error: (error) => {
          // User hasn't reviewed this show yet, which is fine
          this.userReview = null;
        }
      });
    }
  }

  formatSchedule(): string {
    const parts: string[] = [];
    if (this.show?.scheduleDays?.length) {
      parts.push(this.show.scheduleDays.join(', '));
    }
    if (this.show?.scheduleTime) {
      parts.push(`at ${this.show.scheduleTime}`);
    }
    return parts.join(' ') || 'Not scheduled';
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  addToWatchlist(): void {
    if (!this.show || !this.currentUser?.username) return;

    this.addingToWatchlist = true;
    this.tvShowService.addToWatchlist(this.show.id, this.currentUser.username).subscribe({
      next: () => {
        alert('Added to watchlist!');
      },
      error: (error) => {
        alert('Error adding to watchlist: ' + (error.error?.message || 'Unknown error'));
      },
      complete: () => {
        this.addingToWatchlist = false;
      }
    });
  }

  openReviewModal(): void {
    this.isReviewModalOpen = true;
  }

  closeReviewModal(): void {
    this.isReviewModalOpen = false;
  }

  onReviewSubmitted(): void {
    // Reload all review data after submission
    if (this.show) {
      this.loadReviewStats(this.show.id);
      this.loadCommunityReviews(this.show.id);
      this.loadUserReview(this.show.id);
    }
  }

  deleteReview(): void {
    if (!this.userReview) return;

    if (confirm('Are you sure you want to delete your review?')) {
      this.reviewService.deleteReview(this.userReview.id).subscribe({
        next: () => {
          this.userReview = null;
          this.loadReviewStats(this.show!.id);
          this.loadCommunityReviews(this.show!.id);
        },
        error: (error) => {
          alert('Error deleting review: ' + (error.error?.message || 'Unknown error'));
        }
      });
    }
  }

  goToUpgrade(): void {
    this.router.navigate(['/upgrade']);
  }
}