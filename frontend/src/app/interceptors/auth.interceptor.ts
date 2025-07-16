import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const currentUser = authService.getCurrentUser();

    if (currentUser?.token) {
        console.log(currentUser?.membership)
        const authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${currentUser.token}`,
                'X-Auth-Username': currentUser.username,
                'X-Auth-Role': currentUser?.role || "USER",
                'X-Auth-Membership': currentUser?.membership || "FREE"
            }
        });

        return next(authReq).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401 && !req.url.includes('/auth/refresh') && !req.url.includes('/auth/login')) {
                    // Token expired, try to refresh
                    return authService.refreshToken().pipe(
                        switchMap(() => {
                            // Retry original request with new token
                            const newUser = authService.getCurrentUser();
                            console.log(newUser);
                            const retryReq = req.clone({
                                setHeaders: {
                                    Authorization: `Bearer ${newUser?.token}`,
                                    'X-Auth-Username': newUser?.username || '',
                                    'X-Auth-Role': newUser?.role || "USER",
                                    'X-Auth-Membership': newUser?.membership || "FREE"
                                }
                            });
                            return next(retryReq);
                        }),
                        catchError((refreshError) => {
                            // Refresh failed, logout user
                            authService.logout();
                            return throwError(() => refreshError);
                        })
                    );
                }
                return throwError(() => error);
            })
        );
    }

    return next(req);
};