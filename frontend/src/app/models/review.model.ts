export interface Review {
    id: string;
    username: string;
    tvShowId: number;
    content: string;
    rating: number;
    isApproved: boolean;
    isFlagged: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface StoreReviewRequest {
    content: string;
    rating: number;
}

export interface UpdateReviewRequest {
    content: string;
    rating: number;
}

export interface ReviewStats {
    tvShowId: number;
    averageRating: number;
    totalReviews: number;
}

export interface PagedReviews {
    content: Review[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}