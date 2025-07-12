export interface Review {
    id: string;
    username: string;
    tvShowId: number;
    content: string;
    rating: number;
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