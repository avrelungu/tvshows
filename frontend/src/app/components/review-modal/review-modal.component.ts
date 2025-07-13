import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { Review, StoreReviewRequest, UpdateReviewRequest } from '../../models/review.model';

@Component({
  selector: 'app-review-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-overlay" (click)="onClose()" *ngIf="isOpen">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Edit Your Review' : 'Write a Review' }}</h2>
          <button class="close-btn" (click)="onClose()">×</button>
        </div>
        
        <div class="modal-body">
          <div class="show-info" *ngIf="showName">
            <h3>{{ showName }}</h3>
          </div>
          
          <form (ngSubmit)="onSubmit()" #reviewForm="ngForm">
            <div class="form-group">
              <label for="rating">Rating</label>
              <div class="star-rating">
                <button
                  type="button"
                  *ngFor="let star of [1,2,3,4,5]; let i = index"
                  class="star-btn"
                  [class.active]="i < rating"
                  [class.hover]="i < hoverRating"
                  (click)="setRating(i + 1)"
                  (mouseenter)="setHoverRating(i + 1)"
                  (mouseleave)="clearHoverRating()">
                  ⭐
                </button>
              </div>
              <span class="rating-text">{{ getRatingText() }}</span>
            </div>
            
            <div class="form-group">
              <label for="content">Your Review</label>
              <textarea
                id="content"
                name="content"
                [(ngModel)]="content"
                placeholder="Share your thoughts about this show..."
                rows="6"
                maxlength="1000"
                required
                #contentInput="ngModel">
              </textarea>
              <div class="char-count">{{ content.length }}/1000</div>
              <div class="error" *ngIf="contentInput.invalid && contentInput.touched">
                Please write a review
              </div>
            </div>
            
            <div class="modal-actions">
              <button type="button" class="btn btn-secondary" (click)="onClose()">
                Cancel
              </button>
              <button 
                type="submit" 
                class="btn btn-primary"
                [disabled]="!reviewForm.valid || rating === 0 || isSubmitting">
                {{ isSubmitting ? 'Saving...' : (isEditing ? 'Update Review' : 'Submit Review') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 600px;
      max-height: 90vh;
      overflow-y: auto;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #e5e7eb;
    }

    .modal-header h2 {
      margin: 0;
      color: #1f2937;
      font-size: 1.5rem;
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      color: #6b7280;
      padding: 0;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background-color 0.2s ease;
    }

    .close-btn:hover {
      background: #f3f4f6;
    }

    .modal-body {
      padding: 1.5rem;
    }

    .show-info {
      margin-bottom: 1.5rem;
      padding: 1rem;
      background: #f8fafc;
      border-radius: 8px;
    }

    .show-info h3 {
      margin: 0;
      color: #1f2937;
      font-size: 1.125rem;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    .form-group label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 600;
      color: #374151;
    }

    .star-rating {
      display: flex;
      gap: 0.25rem;
      margin-bottom: 0.5rem;
    }

    .star-btn {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      padding: 0.25rem;
      border-radius: 4px;
      transition: all 0.2s ease;
      color: #d1d5db;
    }

    .star-btn.active,
    .star-btn.hover {
      color: #fbbf24;
      transform: scale(1.1);
    }

    .rating-text {
      font-size: 0.875rem;
      color: #6b7280;
      font-weight: 500;
    }

    textarea {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-family: inherit;
      font-size: 0.875rem;
      line-height: 1.5;
      resize: vertical;
      min-height: 120px;
    }

    textarea:focus {
      outline: none;
      border-color: #4f46e5;
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
    }

    .char-count {
      text-align: right;
      font-size: 0.75rem;
      color: #6b7280;
      margin-top: 0.25rem;
    }

    .error {
      color: #ef4444;
      font-size: 0.75rem;
      margin-top: 0.25rem;
    }

    .modal-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      margin-top: 2rem;
    }

    .btn {
      padding: 0.75rem 1.5rem;
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

    .btn-secondary {
      background: #f3f4f6;
      color: #374151;
    }

    .btn-secondary:hover:not(:disabled) {
      background: #e5e7eb;
    }

    .btn-primary {
      background: #4f46e5;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background: #4338ca;
    }

    @media (max-width: 640px) {
      .modal-content {
        width: 95%;
        margin: 1rem;
      }
      
      .modal-actions {
        flex-direction: column;
      }
      
      .btn {
        width: 100%;
      }
    }
  `]
})
export class ReviewModalComponent implements OnInit {
  @Input() isOpen = false;
  @Input() tvShowId: number | null = null;
  @Input() showName: string = '';
  @Input() existingReview: Review | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() reviewSubmitted = new EventEmitter<void>();

  rating = 0;
  hoverRating = 0;
  content = '';
  isSubmitting = false;
  currentUser: any;

  constructor(
    private reviewService: ReviewService,
    private authService: AuthService
  ) {
    this.currentUser = this.authService.getCurrentUser();
  }

  ngOnInit(): void {
    if (this.existingReview) {
      this.rating = this.existingReview.rating;
      this.content = this.existingReview.content;
    }
  }

  get isEditing(): boolean {
    return !!this.existingReview;
  }

  setRating(rating: number): void {
    this.rating = rating;
  }

  setHoverRating(rating: number): void {
    this.hoverRating = rating;
  }

  clearHoverRating(): void {
    this.hoverRating = 0;
  }

  getRatingText(): string {
    const ratingTexts = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];
    return ratingTexts[this.rating] || '';
  }

  onSubmit(): void {
    if (!this.tvShowId || !this.currentUser?.username || this.rating === 0 || !this.content.trim()) {
      return;
    }

    this.isSubmitting = true;

    if (this.isEditing && this.existingReview) {
      // Update existing review
      const updateData: UpdateReviewRequest = {
        content: this.content.trim(),
        rating: this.rating
      };

      this.reviewService.updateReview(this.existingReview.id, updateData).subscribe({
        next: () => {
          this.reviewSubmitted.emit();
          this.onClose();
        },
        error: (error) => {
          console.error('Error updating review:', error);
          alert('Error updating review: ' + (error.error?.message || 'Unknown error'));
        },
        complete: () => {
          this.isSubmitting = false;
        }
      });
    } else {
      // Create new review
      const reviewData: StoreReviewRequest = {
        content: this.content.trim(),
        rating: this.rating
      };

      this.reviewService.createReview(this.tvShowId, this.currentUser.username, reviewData).subscribe({
        next: () => {
          this.reviewSubmitted.emit();
          this.onClose();
        },
        error: (error) => {
          console.error('Error creating review:', error);
          if (error.error?.message?.includes('already has a review')) {
            alert('You have already reviewed this show. You can only write one review per show.');
          } else {
            alert('Error creating review: ' + (error.error?.message || 'Unknown error'));
          }
        },
        complete: () => {
          this.isSubmitting = false;
        }
      });
    }
  }

  onClose(): void {
    this.close.emit();
    this.resetForm();
  }

  private resetForm(): void {
    if (!this.existingReview) {
      this.rating = 0;
      this.content = '';
    }
    this.hoverRating = 0;
    this.isSubmitting = false;
  }
}