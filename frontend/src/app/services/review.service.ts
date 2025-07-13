import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review, StoreReviewRequest, UpdateReviewRequest, ReviewStats, PagedReviews } from '../models/review.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ReviewService {
    private apiUrl = `${environment.apiUrl}/review`;

    constructor(private http: HttpClient) {}

    createReview(tvShowId: number, username: string, reviewData: StoreReviewRequest): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${tvShowId}/${username}`, reviewData);
    }

    getUserReviews(username: string): Observable<Review[]> {
        return this.http.get<Review[]>(`${this.apiUrl}/${username}`);
    }

    updateReview(reviewId: string, reviewData: UpdateReviewRequest): Observable<Review> {
        return this.http.put<Review>(`${this.apiUrl}/${reviewId}`, reviewData);
    }

    getReviewsByTvShow(tvShowId: number, page: number = 0, size: number = 10): Observable<PagedReviews> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString())
            .set('sortBy', 'createdAt')
            .set('sortDir', 'desc');
        
        return this.http.get<PagedReviews>(`${this.apiUrl}/tv-show/${tvShowId}`, { params });
    }

    getReviewStats(tvShowId: number): Observable<ReviewStats> {
        return this.http.get<ReviewStats>(`${this.apiUrl}/tv-show/${tvShowId}/stats`);
    }

    getUserReviewForShow(tvShowId: number, username: string): Observable<Review> {
        return this.http.get<Review>(`${this.apiUrl}/tv-show/${tvShowId}/user/${username}`);
    }

    deleteReview(reviewId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${reviewId}`);
    }

    getPendingReviews(): Observable<Review[]> {
        return this.http.get<Review[]>(`${this.apiUrl}/pending`);
    }

    approveReview(reviewId: string): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${reviewId}/approve`, {});
    }

    rejectReview(reviewId: string): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${reviewId}/reject`, {});
    }
}