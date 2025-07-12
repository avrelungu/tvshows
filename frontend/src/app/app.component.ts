import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, RouterLinkActive],
  template: `
    <div class="app">
      <nav class="main-nav" *ngIf="showNavigation">
        <div class="nav-brand">
          <h1>TV Shows App</h1>
        </div>
        <div class="nav-links" *ngIf="isAuthenticated">
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/reviews" routerLinkActive="active">Reviews</a>
          <a
              routerLink="/upgrade"
              routerLinkActive="active"
              class="upgrade-link"
              *ngIf="currentUser?.role === 'FREE'">
            Upgrade to Premium
          </a>
          <button (click)="logout()" class="logout-btn">Logout</button>
        </div>
      </nav>

      <main class="main-content" [class.no-nav]="!showNavigation">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .main-nav {
      background: #2c3e50;
      color: white;
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .nav-brand h1 {
      margin: 0;
      font-size: 1.5rem;
    }

    .nav-links {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .nav-links a {
      color: white;
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s ease;
    }

    .nav-links a:hover,
    .nav-links a.active {
      background-color: rgba(255, 255, 255, 0.1);
    }

    .logout-btn {
      background: #e74c3c;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      transition: background-color 0.3s ease;
    }

    .logout-btn:hover {
      background: #c0392b;
    }

    .main-content {
      flex: 1;
    }

    .main-content.no-nav {
      min-height: 100vh;
    }

    .nav-links a.upgrade-link {
      background: #f39c12;
      border-radius: 20px;
      padding: 0.5rem 1rem;
      font-weight: bold;
      transition: all 0.3s ease;
    }

    .nav-links a.upgrade-link:hover {
      background: #e67e22;
      transform: translateY(-1px);
    }
  `]
})
export class AppComponent {
  showNavigation = true;
  isAuthenticated = false;
  currentUser: any = null;

  constructor(
      private authService: AuthService,
      private router: Router
  ) {
    // Subscribe to authentication state
    this.authService.currentUser$.subscribe(user => {
      this.isAuthenticated = !!user;
      this.currentUser = user;
    });

    // Hide navigation on login/register pages
    this.router.events.pipe(
        filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.showNavigation = !['/login', '/register'].includes(event.url);
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}