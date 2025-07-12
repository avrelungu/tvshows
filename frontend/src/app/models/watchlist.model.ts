export interface WatchlistItem {
    id: string;
    showId: number;
    name: string;
    description: string;
    imageMedium: string;
    imageOriginal: string;
    createdAt?: string;
}

export interface StoreWatchlistRequest {
    tvShowId: number;
    name: string;
    description: string;
    imageMedium: string;
    imageOriginal: string;
}