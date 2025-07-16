import { Component } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { UserDropdownComponent } from './components/user-dropdown/user-dropdown.component';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, UserDropdownComponent],
  template: `
    <div class="app">
      <nav class="main-nav" *ngIf="showNavigation">
        <div class="nav-brand">
          <h1>TV Shows App</h1>
        </div>
        <div class="nav-user" *ngIf="isAuthenticated">
          <app-user-dropdown 
            [username]="currentUser?.username || ''"
            [userMembershipType]="currentUser?.membership || ''"
            [userRole]="currentUser?.role || ''"
            (logout)="logout()">
          </app-user-dropdown>
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

    .nav-user {
      display: flex;
      align-items: center;
    }

    .main-content {
      flex: 1;
    }

    .main-content.no-nav {
      min-height: 100vh;
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