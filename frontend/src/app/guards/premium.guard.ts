import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
    providedIn: 'root'
})
export class PremiumGuard implements CanActivate {
    constructor(private authService: AuthService, private router: Router) {}

    canActivate(): boolean {
        const user = this.authService.getCurrentUser();
        if (user && user.role === 'ADMIN') {
            return true;
        }

        this.router.navigate(['/upgrade']);
        return false;
    }
}