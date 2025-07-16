import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UpgradeUserProfileDto {
    username: string;
}

export interface UserProfileDto {
    id: string;
    username: string;
    email: string;
    membership: string;
    firstName: string;
    lastName: string;
}

@Injectable({
    providedIn: 'root'
})
export class UpgradeService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) {}

    upgradeUserToPremium(username: string): Observable<UserProfileDto> {
        const upgradeRequest: UpgradeUserProfileDto = {
            username: username
        };

        // First call user service to upgrade profile
        return this.http.post<UserProfileDto>(`${this.apiUrl}/users/upgrade`, upgradeRequest).pipe(
            switchMap((userProfile) => {
                // If user service upgrade succeeds, also upgrade in auth service
                return this.http.post<void>(`${this.apiUrl}/auth/users/${username}/upgrade`, {}).pipe(
                    switchMap(() => {
                        // Return the user profile from the first call
                        return new Observable<UserProfileDto>(subscriber => {
                            subscriber.next(userProfile);
                            subscriber.complete();
                        });
                    })
                );
            })
        );
    }
}