import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-user-dropdown',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="user-dropdown" [class.open]="isOpen">
      <button class="user-avatar" (click)="toggleDropdown()">
        <span class="initials">{{ userInitials }}</span>
        <span class="role-badge" [class]="userRole?.toLowerCase()">{{ userRole }}</span>
      </button>
      
      <div class="dropdown-menu" *ngIf="isOpen">
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <span class="role">{{ userRole }}</span>
        </div>
        <div class="menu-divider"></div>
        <a routerLink="/dashboard" (click)="closeDropdown()" class="menu-item">
          <span class="menu-icon">📊</span>
          Dashboard
        </a>
        <a routerLink="/reviews" (click)="closeDropdown()" class="menu-item">
          <span class="menu-icon">⭐</span>
          Reviews
        </a>
        <a 
          routerLink="/upgrade" 
          (click)="closeDropdown()" 
          class="menu-item upgrade-item"
          *ngIf="userRole === 'FREE'">
          <span class="menu-icon">⬆️</span>
          Upgrade to Premium
        </a>
        <div class="menu-divider"></div>
        <button (click)="onLogout()" class="menu-item logout-item">
          <span class="menu-icon">🚪</span>
          Logout
        </button>
      </div>
    </div>
    <div class="dropdown-overlay" *ngIf="isOpen" (click)="closeDropdown()"></div>
  `,
  styles: [`
    .user-dropdown {
      position: relative;
      z-index: 1000;
    }

    .user-avatar {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: #4f46e5;
      border: none;
      border-radius: 50px;
      padding: 0.5rem 1rem;
      cursor: pointer;
      transition: all 0.3s ease;
      position: relative;
    }

    .user-avatar:hover {
      background: #4338ca;
      transform: translateY(-1px);
    }

    .initials {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: white;
      color: #4f46e5;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 0.875rem;
    }

    .role-badge {
      color: white;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .role-badge.free {
      color: #fbbf24;
    }

    .role-badge.premium {
      color: #10b981;
    }

    .role-badge.admin {
      color: #f59e0b;
    }

    .dropdown-menu {
      position: absolute;
      top: calc(100% + 0.5rem);
      right: 0;
      background: white;
      border-radius: 12px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
      min-width: 200px;
      padding: 0.5rem 0;
      border: 1px solid #e5e7eb;
      animation: dropdownSlide 0.2s ease-out;
    }

    @keyframes dropdownSlide {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .user-info {
      padding: 1rem;
      text-align: center;
    }

    .username {
      display: block;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 0.25rem;
    }

    .role {
      font-size: 0.75rem;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .menu-divider {
      height: 1px;
      background: #e5e7eb;
      margin: 0.5rem 0;
    }

    .menu-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      color: #374151;
      text-decoration: none;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      cursor: pointer;
      transition: background-color 0.2s ease;
      font-size: 0.875rem;
    }

    .menu-item:hover {
      background: #f3f4f6;
    }

    .menu-icon {
      font-size: 1rem;
      width: 20px;
      text-align: center;
    }

    .upgrade-item {
      color: #f59e0b;
      font-weight: 500;
    }

    .upgrade-item:hover {
      background: #fef3c7;
    }

    .logout-item {
      color: #dc2626;
    }

    .logout-item:hover {
      background: #fef2f2;
    }

    .dropdown-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 999;
    }
  `]
})
export class UserDropdownComponent {
  @Input() username: string = '';
  @Input() userRole: string = '';
  @Output() logout = new EventEmitter<void>();

  isOpen = false;

  get userInitials(): string {
    if (!this.username) return 'U';
    
    const names = this.username.split(' ');
    if (names.length >= 2) {
      return (names[0][0] + names[1][0]).toUpperCase();
    }
    return this.username.slice(0, 2).toUpperCase();
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
  }

  closeDropdown(): void {
    this.isOpen = false;
  }

  onLogout(): void {
    this.closeDropdown();
    this.logout.emit();
  }
}