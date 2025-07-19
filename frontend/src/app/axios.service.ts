import { Injectable } from '@angular/core';
import axios, { AxiosInstance } from 'axios';
import { environment } from '../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AxiosService {
    private axiosInstance: AxiosInstance;

    constructor() {
        this.axiosInstance = axios.create({
            baseURL: environment.apiUrl.replace('/api', ''), // Remove /api to get base URL
            headers: {
                'Content-Type': 'application/json',
            },
            timeout: 30000
        });
    }

    private readonly AUTH_TOKEN_KEY = 'auth_token';

    getAuthToken(): string | null {
        return window.localStorage.getItem(this.AUTH_TOKEN_KEY);
    }

    setAuthToken(token: string | null): void {
        if (token !== null) {
            window.localStorage.setItem(this.AUTH_TOKEN_KEY, token);
        } else {
            window.localStorage.removeItem(this.AUTH_TOKEN_KEY);
        }
    }

    request(method: string, url: string, data: any): Promise<any> {
        let headers: any = {};

        const token = this.getAuthToken();
        if (token !== null) {
            headers = {"Authorization": `Bearer ${token}`};
        }

        return this.axiosInstance.request({
            method: method,
            url: url,
            data: data,
            headers: headers
        });
    }
}
