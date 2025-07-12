import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User, LoginUser, SignUpRequest, LoginRequest } from '../models/user.model';
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
}