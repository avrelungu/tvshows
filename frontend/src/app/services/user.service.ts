import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserProfile, UpgradeProfileRequest } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private apiUrl = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) {}

    getUserProfile(username: string): Observable<UserProfile> {
        return this.http.get<UserProfile>(`${this.apiUrl}/${username}/profile`);
    }

    upgradeProfile(upgradeData: UpgradeProfileRequest): Observable<UserProfile> {
        return this.http.post<UserProfile>(`${this.apiUrl}/upgrade`, upgradeData);
    }
}