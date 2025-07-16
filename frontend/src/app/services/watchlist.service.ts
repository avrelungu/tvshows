import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, map } from 'rxjs';
import { WatchlistItem, StoreWatchlistRequest } from '../models/watchlist.model';
import { TvShow, PageResponse } from '../models/tvshow.model';
import { environment } from '../../environments/environment';
import { TvShowService } from "./tvshow.service";

@Injectable({
    providedIn: 'root'
})
export class WatchlistService {
    private apiUrl = `${environment.apiUrl}/users/watchlist`;

    constructor(
        private http: HttpClient,
        private tvShowsService: TvShowService
    ) {}

    getWatchlist(username: string): Observable<WatchlistItem[]> {
        return this.http.get<WatchlistItem[]>(`${this.apiUrl}/${username}`);
    }

    getWatchlistShows(username: string, page = 0, size = 10): Observable<PageResponse<TvShow>> {
        return this.getWatchlist(username).pipe(
            switchMap(watchlistItems => {
                if (watchlistItems.length === 0) {
                    return new Observable<PageResponse<TvShow>>(observer => {
                        observer.next({
                            content: [],
                            page:{
                                totalElements: 0,
                                totalPages: 0,
                                size: size,
                                number: page
                            }
                        });
                        observer.complete();
                    });
                }

                const showIds = watchlistItems.map(item => item.showId);
                return this.tvShowsService.getTvShows(page, size, { ids: showIds });
            })
        );
    }

    addToWatchlist(username: string, watchlistData: StoreWatchlistRequest): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${username}`, watchlistData);
    }

    removeFromWatchlist(username: string, showId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${username}/${showId}`);
    }
}