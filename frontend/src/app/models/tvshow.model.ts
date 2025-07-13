export interface TvShow {
    id: number;
    name: string;
    language: string;
    status: string;
    rating: number;
    imageMedium: string;
    imageOriginal: string;
    summary: string;
    genres: string[];
    scheduleTime: string;
    scheduleDays: string[];
    runtime: number;
    averageRuntime: number;
    premiered: string;
    ended: string;
    officialSite: string;
    tvrage: number;
    thetvdb: number;
    imdb: string;
    watchlistUrl?: string;
    reviewUrl?: string;
}

export interface TvShowFilter {
    name?: string;
    description?: string;
    premiered?: string;
    ended?: string;
    minRating?: number;
    maxRating?: number;
    status?: string;
    network?: string;
    language?: string;
    sortBy?: string;
    sortOrder?: string;
    genres?: string[];
    ids?: number[];
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}