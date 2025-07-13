import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const currentUser = authService.getCurrentUser();

    if (currentUser?.token) {
        const authReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${currentUser.token}`,
                'X-Auth-Username': currentUser.username,
                'X-Auth-Role': currentUser.role || "FREE"
            }
        });
        return next(authReq);
    }

    return next(req);
};