import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Review, StoreReviewRequest, UpdateReviewRequest } from '../models/review.model';
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
}