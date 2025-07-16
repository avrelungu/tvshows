import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User, LoginUser, SignUpRequest, LoginRequest, RefreshTokenRequest } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = environment.apiUrl;
    private currentUserSubject = new BehaviorSubject<LoginUser | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) {
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            this.currentUserSubject.next(JSON.parse(savedUser));
        }
    }

    login(credentials: LoginRequest): Observable<LoginUser> {
        return this.http.post<LoginUser>(`${this.apiUrl}/auth/login`, credentials)
            .pipe(
                tap(user => {
                    localStorage.setItem('currentUser', JSON.stringify(user));
                    this.currentUserSubject.next(user);
                })
            );
    }

    register(signUpData: SignUpRequest): Observable<User> {
        return this.http.post<User>(`${this.apiUrl}/auth/register`, signUpData);
    }

    logout(): void {
        localStorage.removeItem('currentUser');
        this.currentUserSubject.next(null);
    }

    getCurrentUser(): LoginUser | null {
        return this.currentUserSubject.value;
    }

    isAuthenticated(): boolean {
        return !!this.getCurrentUser()?.token;
    }

    getUser(username: string): Observable<User> {
        return this.http.get<User>(`${this.apiUrl}/auth/${username}`);
    }

    refreshToken(): Observable<LoginUser> {
        const currentUser = this.getCurrentUser();
        if (!currentUser?.refreshToken) {
            throw new Error('No refresh token available');
        }

        const refreshRequest: RefreshTokenRequest = {
            refreshToken: currentUser.refreshToken,
        };

        return this.http.post<LoginUser>(`${this.apiUrl}/auth/refresh`, refreshRequest)
            .pipe(
                tap(user => {
                    console.log('Refresh token refreshed', user);
                    localStorage.setItem('currentUser', JSON.stringify(user));
                    this.currentUserSubject.next(user);
                })
            );
    }

    getAccessToken(): string | null {
        return this.getCurrentUser()?.token || null;
    }

    getRefreshToken(): string | null {
        return this.getCurrentUser()?.refreshToken || null;
    }

    getAllUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.apiUrl}/auth/users`);
    }

    promoteToAdmin(username: string): Observable<User> {
        return this.http.post<User>(`${this.apiUrl}/auth/promote/${username}`, {});
    }

    // Force refresh user token to get updated membership/role info
    refreshUserToken(): Observable<LoginUser> {
        return this.refreshToken().pipe(
            tap(user => {
                // Update current user with new token and membership info
                this.currentUserSubject.next(user);
            })
        );
    }
}