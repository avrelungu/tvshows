import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WatchlistItem, StoreWatchlistRequest } from '../models/watchlist.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class WatchlistService {
    private apiUrl = `${environment.apiUrl}/users/watchlist`;

    constructor(private http: HttpClient) {}

    getWatchlist(username: string): Observable<WatchlistItem[]> {
        return this.http.get<WatchlistItem[]>(`${this.apiUrl}/${username}`);
    }

    addToWatchlist(username: string, watchlistData: StoreWatchlistRequest): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${username}`, watchlistData);
    }
}