import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TvShow, TvShowFilter, PageResponse } from '../models/tvshow.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class TvShowService {
    private apiUrl = `${environment.apiUrl}/tv-shows`;

    constructor(private http: HttpClient) {}

    getTvShows(page = 0, size = 10, filter?: TvShowFilter): Observable<PageResponse<TvShow>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (filter) {
            Object.keys(filter).forEach(key => {
                const value = filter[key as keyof TvShowFilter];
                if (value !== undefined && value !== null) {
                    if (Array.isArray(value)) {
                        value.forEach(v => params = params.append(key, v));
                    } else {
                        params = params.set(key, value.toString());
                    }
                }
            });
        }

        return this.http.get<PageResponse<TvShow>>(this.apiUrl, { params });
    }

    getTopRatedShows(page = 0, size = 10): Observable<PageResponse<TvShow>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        return this.http.get<PageResponse<TvShow>>(`${this.apiUrl}/top-rated`, { params });
    }

    addToWatchlist(tvShowId: number, username: string): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/${tvShowId}/watchlist/${username}`, {});
    }
}