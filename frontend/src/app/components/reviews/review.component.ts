import { Component, OnInit } from '@angular/core';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';
import { Review, StoreReviewRequest } from '../../models/review.model';
import {DatePipe, NgForOf, NgIf} from "@angular/common";

@Component({
    selector: 'app-reviews',
    template: `
        <div class="reviews-container">
            <h2>My Reviews</h2>

            <div class="reviews-list" *ngIf="reviews.length > 0">
                <div class="review-card" *ngFor="let review of reviews">
                    <div class="review-header">
                        <h3>TV Show ID: {{ review.tvShowId }}</h3>
                        <div class="rating">
                            <span *ngFor="let star of getStarArray(review.rating)">⭐</span>
                            <span class="rating-text">{{ review.rating }}/5</span>
                        </div>
                    </div>
                    <p class="review-content">{{ review.content }}</p>
                    <div class="review-meta">
                        <small>Posted on {{ review.createdAt | date }}</small>
                        <button (click)="editReview(review)" class="edit-btn">Edit</button>
                    </div>
                </div>
            </div>

            <div *ngIf="reviews.length === 0" class="empty-state">
                <p>You haven't written any reviews yet.</p>
            </div>
        </div>
    `,
    imports: [
        NgForOf,
        NgIf,
        DatePipe
    ],
    styles: [`
        .reviews-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 2rem;
        }

        .review-card {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            margin-bottom: 1rem;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .review-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .rating {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .review-content {
            line-height: 1.6;
            margin-bottom: 1rem;
        }

        .review-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: #666;
        }

        .edit-btn {
            padding: 0.25rem 0.5rem;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.875rem;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #666;
        }
    `]
})
export class ReviewsComponent implements OnInit {
    reviews: Review[] = [];
    currentUser: any;

    constructor(
        private reviewService: ReviewService,
        private authService: AuthService
    ) {}

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        this.loadReviews();
    }

    loadReviews(): void {
        if (this.currentUser?.username) {
            this.reviewService.getUserReviews(this.currentUser.username).subscribe({
                next: (reviews) => {
                    this.reviews = reviews;
                },
                error: (error) => {
                    console.error('Error loading reviews:', error);
                }
            });
        }
    }

    getStarArray(rating: number): number[] {
        return Array(rating).fill(0);
    }

    editReview(review: Review): void {
        // Implementation for editing reviews
        console.log('Edit review:', review);
    }
}