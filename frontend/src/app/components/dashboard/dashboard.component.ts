import { Component, OnInit, OnDestroy } from '@angular/core';
import { TvShowService } from '../../services/tvshow.service';
import { WatchlistService } from '../../services/watchlist.service';
import { AuthService } from '../../services/auth.service';
import { UpgradeService } from '../../services/upgrade.service';
import { TvShow, PageResponse } from '../../models/tvshow.model';
import { WatchlistItem } from '../../models/watchlist.model';
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Router} from "@angular/router";
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
    selector: 'app-dashboard',
    template: `
        <div class="dashboard">
            <header class="dashboard-header">
                <h1>TV Shows Dashboard</h1>
            </header>

            <nav class="dashboard-nav">
                <button
                        *ngFor="let section of sections"
                        [class.active]="activeSection === section.key"
                        (click)="setActiveSection(section.key)">
                    {{ section.label }}
                </button>
            </nav>

            <main class="dashboard-content">
                <!-- Top Rated Shows Section -->
                <section *ngIf="activeSection === 'top-rated'">
                    <h2>Top Rated Shows</h2>
                    <div class="shows-grid" *ngIf="topRatedShows && topRatedShows.content">
                        <div class="show-card" *ngFor="let show of topRatedShows.content" (click)="goToShowDetails(show.id)">
                            <img [src]="show.imageMedium" [alt]="show.name"/>
                            <div class="show-info">
                                <h3>{{ show.name }}</h3>
                                <p class="rating">⭐ {{ show.rating }}/10</p>
                                <p class="genres">{{ show.genres.join(', ') || 'No genres' }}</p>
                                <button (click)="addToWatchlist(show)" [disabled]="addingToWatchlist" (click)="$event.stopPropagation()">
                                    Add to Watchlist
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Browse Shows Section -->
                <section *ngIf="activeSection === 'browse'">
                    <h2>Browse TV Shows</h2>

                    <!-- Search Filters -->
                    <div class="filters">
                        <div class="search-input-container">
                            <input
                                    type="text"
                                    placeholder="Search shows..."
                                    [(ngModel)]="searchFilter.name"
                                    (input)="onNameFilterChange()">
                            <span *ngIf="isSearching" class="search-loading">🔍</span>
                        </div>

                        <select [(ngModel)]="searchFilter.status" (change)="onFilterChange()">
                            <option value="" disabled selected>Status</option>
                            <option value="">All Status</option>
                            <option value="Running">Running</option>
                            <option value="Ended">Ended</option>
                        </select>

                        <select [(ngModel)]="searchFilter.language" (change)="onFilterChange()">
                            <option value="" disabled selected>Language</option>
                            <option value="">All Languages</option>
                            <option value="English">English</option>
                            <option value="Spanish">Spanish</option>
                            <option value="French">French</option>
                        </select>

                        <button (click)="clearFilters()">Clear Filters</button>
                    </div>

                    <div class="shows-grid" *ngIf="allShows && allShows.content">
                        <div class="show-card" *ngFor="let show of allShows.content" (click)="goToShowDetails(show.id)">
                            <img [src]="show.imageMedium" [alt]="show.name"/>
                            <div class="show-info">
                                <h3>{{ show.name }}</h3>
                                <p class="rating">⭐ {{ show.rating }}/10</p>
                                <p class="status">{{ show.status }}</p>
                                <p class="genres">{{ show.genres.join(', ') || 'No genres' }}</p>
                                <button (click)="addToWatchlist(show)" [disabled]="addingToWatchlist" (click)="$event.stopPropagation()">
                                    Add to Watchlist
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Pagination -->
                    <div class="load-more-section" *ngIf="allShows">
                        <button 
                                *ngIf="hasMorePages"
                                [disabled]="loadingMore"
                                (click)="loadMoreShows()"
                                class="load-more-btn">
                            {{ loadingMore ? 'Loading...' : 'Load More Shows' }}
                        </button>
                        <p *ngIf="!hasMorePages && allShows.content.length > 0" class="end-message">
                            You've seen all {{ allShows.page.totalElements }} shows!
                        </p>
                    </div>
                </section>

                <!-- Watchlist Section -->
                <section *ngIf="activeSection === 'watchlist'">
                    <div class="section-header">
                        <h2>My Watchlist</h2>
                        <div class="watchlist-info" *ngIf="currentUser?.membership === 'FREE'">
                            <span class="watchlist-limit">{{ watchlist?.content?.length || 0 }}/10 shows</span>
                            <button class="upgrade-btn" (click)="goToUpgrade()">
                                Upgrade for Unlimited
                            </button>
                        </div>
                    </div>

                    <!-- Upgrade Notice for FREE users approaching limit -->
                    <div class="upgrade-notice" *ngIf="currentUser?.membership === 'FREE' && (watchlist?.content?.length || 0) >= 8">
                        <div class="notice-content">
                            <h3>Almost at your limit!</h3>
                            <p>You have {{ 10 - (watchlist?.content?.length || 0) }} watchlist slots remaining.</p>
                            <button (click)="goToUpgrade()" class="upgrade-cta">
                                Upgrade to Premium for unlimited watchlist
                            </button>
                        </div>
                    </div>

                    <div class="shows-grid" *ngIf="watchlist && watchlist.content && watchlist.content.length > 0">
                        <div class="show-card" *ngFor="let show of watchlist.content" (click)="goToShowDetails(show.id)">
                            <img [src]="show.imageMedium" [alt]="show.name"/>
                            <div class="show-info">
                                <h3>{{ show.name }}</h3>
                                <p class="rating">⭐ {{ show.rating }}/10</p>
                                <p class="status">{{ show.status }}</p>
                                <p class="genres">{{ show.genres.join(', ') || 'No genres' }}</p>
                                <button (click)="removeFromWatchlist(show)" (click)="$event.stopPropagation()">
                                    Remove from Watchlist
                                </button>
                            </div>
                        </div>
                    </div>
                    <div *ngIf="watchlist && watchlist.content && watchlist.content.length === 0" class="empty-state">
                        <p>Your watchlist is empty. Add some shows to get started!</p>
                    </div>
                </section>
            </main>
        </div>
    `,
    imports: [
        NgForOf,
        NgIf,
        FormsModule
    ],
    styles: [`
        .dashboard {
            min-height: 100vh;
            background: #f5f5f5;
        }

        .dashboard-header {
            background: white;
            padding: 1rem 2rem;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .user-info {
            display: flex;
            align-items: center;
            gap: 1rem;
            position: relative;
        }

        .user-dropdown {
            position: relative;
        }

        .user-greeting {
            cursor: pointer;
            padding: 0.5rem;
            border-radius: 4px;
            transition: background-color 0.2s ease;
        }

        .user-greeting:hover {
            background: rgba(255, 255, 255, 0.1);
        }

        .dropdown-toggle {
            background: #667eea;
            color: white;
            border: none;
            padding: 0.5rem;
            border-radius: 50%;
            cursor: pointer;
            font-size: 1rem;
            transition: all 0.2s ease;
            width: 35px;
            height: 35px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .dropdown-toggle:hover {
            background: #5a67d8;
            transform: scale(1.05);
        }

        .dropdown-menu {
            position: absolute;
            top: 100%;
            right: 0;
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            min-width: 180px;
            z-index: 1000;
            margin-top: 0.5rem;
        }

        .dropdown-item {
            display: block;
            width: 100%;
            padding: 0.75rem 1rem;
            border: none;
            background: none;
            text-align: left;
            cursor: pointer;
            transition: background-color 0.2s ease;
            font-size: 0.875rem;
            color: #374151;
        }

        .dropdown-item:hover {
            background: #f3f4f6;
        }

        .dropdown-item:first-child {
            border-radius: 8px 8px 0 0;
        }

        .dropdown-item:last-child {
            border-radius: 0 0 8px 8px;
        }

        .dropdown-item.logout {
            color: #dc2626;
            border-top: 1px solid #e5e7eb;
        }

        .dropdown-item.logout:hover {
            background: #fef2f2;
        }

        .member-type {
            background: #667eea;
            color: white;
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.875rem;
        }

        .admin-btn {
            background: #f59e0b;
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.875rem;
            font-weight: 500;
            transition: background-color 0.2s;
        }

        .admin-btn:hover {
            background: #d97706;
        }

        .logout-btn {
            background: #dc2626;
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.875rem;
            font-weight: 500;
            transition: background-color 0.2s;
        }

        .logout-btn:hover {
            background: #b91c1c;
        }

        .dashboard-nav {
            background: white;
            padding: 0 2rem;
            border-bottom: 1px solid #eee;
            display: flex;
            gap: 0;
        }

        .dashboard-nav button {
            padding: 1rem 1.5rem;
            border: none;
            background: none;
            cursor: pointer;
            border-bottom: 3px solid transparent;
            transition: all 0.3s ease;
        }

        .dashboard-nav button.active {
            border-bottom-color: #667eea;
            color: #667eea;
        }

        .dashboard-content {
            padding: 2rem;
        }

        .filters {
            background: white;
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
        }

        .search-input-container {
            position: relative;
            display: flex;
            align-items: center;
        }

        .search-loading {
            position: absolute;
            right: 10px;
            animation: pulse 1.5s infinite;
        }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }

        .filters input, .filters select {
            padding: 0.5rem;
            border: 1px solid #ddd;
            border-radius: 4px;
            min-width: 150px;
        }

        .shows-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 1.5rem;
        }

        .show-card {
            background: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .show-card:hover {
            transform: translateY(-4px);
            cursor: pointer;
        }

        .show-card img {
            width: 100%;
            height: 200px;
            object-fit: cover;
        }

        .show-info {
            padding: 1rem;
        }

        .show-info h3 {
            margin: 0 0 0.5rem 0;
            color: #333;
        }

        .rating {
            color: #f39c12;
            font-weight: bold;
            margin: 0.25rem 0;
        }

        .genres, .status {
            color: #666;
            font-size: 0.875rem;
            margin: 0.25rem 0;
        }

        .show-info button {
            width: 100%;
            padding: 0.5rem;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin-top: 1rem;
        }

        .show-info button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }


        .load-more-section {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-top: 2rem;
            gap: 1rem;
        }

        .load-more-btn {
            padding: 0.75rem 2rem;
            border: 2px solid #007bff;
            background: #007bff;
            color: white;
            cursor: pointer;
            border-radius: 25px;
            font-size: 1rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .load-more-btn:hover:not(:disabled) {
            background: #0056b3;
            border-color: #0056b3;
            transform: translateY(-2px);
        }

        .load-more-btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }

        .end-message {
            color: #666;
            font-style: italic;
            margin: 0;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #666;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .watchlist-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .watchlist-limit {
            background: #f8f9fa;
            padding: 0.5rem 1rem;
            border-radius: 20px;
            font-size: 0.875rem;
            color: #666;
            border: 1px solid #dee2e6;
        }

        .upgrade-btn {
            background: #f39c12;
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.875rem;
            transition: background-color 0.3s ease;
        }

        .upgrade-btn:hover {
            background: #e67e22;
        }

        .upgrade-notice {
            background: linear-gradient(135deg, #f39c12, #e67e22);
            color: white;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            text-align: center;
        }

        .notice-content h3 {
            margin: 0 0 0.5rem 0;
            font-size: 1.2rem;
        }

        .notice-content p {
            margin: 0 0 1rem 0;
            opacity: 0.9;
        }

        .upgrade-cta {
            background: white;
            color: #f39c12;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 6px;
            font-weight: bold;
            cursor: pointer;
            transition: transform 0.2s ease;
        }

        .upgrade-cta:hover {
            transform: translateY(-2px);
        }
    `]
})
export class DashboardComponent implements OnInit, OnDestroy {
    currentUser: any;
    activeSection = 'top-rated';
    sections = [
        { key: 'top-rated', label: 'Top Rated' },
        { key: 'browse', label: 'Browse Shows' },
        { key: 'watchlist', label: 'My Watchlist' }
    ];

    topRatedShows: PageResponse<TvShow> | null = null;
    allShows: PageResponse<TvShow> | null = null;
    watchlist: PageResponse<TvShow> | null = null;

    currentPage = 0;
    pageSize = 12;
    addingToWatchlist = false;
    loadingMore = false;
    hasMorePages = true;
    isSearching = false;
    showUserDropdown = false;
    
    private searchSubject = new Subject<string>();
    private destroy$ = new Subject<void>();

    searchFilter = {
        name: '',
        status: '',
        language: ''
    };

    constructor(
        private tvShowService: TvShowService,
        private watchlistService: WatchlistService,
        private authService: AuthService,
        private upgradeService: UpgradeService,
        private router: Router
    ) {}

    ngOnInit(): void {
        // Subscribe to auth service for real-time user updates
        this.authService.currentUser$.pipe(
            takeUntil(this.destroy$)
        ).subscribe(user => {
            this.currentUser = user;
            // Redirect to login if user becomes null (logged out)
            if (!user) {
                this.router.navigate(['/login']);
            }
        });
        
        this.loadTopRatedShows();
        this.loadAllShows();
        this.loadWatchlist();
        this.setupSearchDebounce();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private setupSearchDebounce(): void {
        this.searchSubject.pipe(
            debounceTime(500),
            distinctUntilChanged(),
            takeUntil(this.destroy$)
        ).subscribe(() => {
            this.isSearching = true;
            this.currentPage = 0;
            this.loadAllShows();
        });
    }

    setActiveSection(section: string): void {
        this.activeSection = section;
    }

    loadTopRatedShows(): void {
        this.tvShowService.getTopRatedShows(0, 12).subscribe({
            next: (shows) => {
                this.topRatedShows = shows;
            },
            error: (error) => {
                console.error('Error loading top rated shows:', error);
            }
        });
    }

    loadAllShows(): void {
        this.currentPage = 0;
        this.hasMorePages = true;
        this.tvShowService.getTvShows(this.currentPage, this.pageSize, this.searchFilter).subscribe({
            next: (shows) => {
                console.log(shows);
                this.allShows = shows;
                this.hasMorePages = shows.page.number < shows.page.totalPages - 1;
                this.isSearching = false;
            },
            error: (error) => {
                console.error('Error loading shows:', error);
                this.isSearching = false;
            }
        });
    }

    loadMoreShows(): void {
        if (!this.hasMorePages || this.loadingMore || !this.allShows) {
            return;
        }

        this.loadingMore = true;
        this.currentPage++;

        this.tvShowService.getTvShows(this.currentPage, this.pageSize, this.searchFilter).subscribe({
            next: (shows) => {
                if (this.allShows) {
                    this.allShows.content = [...this.allShows.content, ...shows.content];
                    this.allShows.page.number = shows.page.number;
                    this.hasMorePages = shows.page.number < shows.page.totalPages - 1;
                }
                this.loadingMore = false;
            },
            error: (error) => {
                console.error('Error loading more shows:', error);
                this.currentPage--; // Revert page increment on error
                this.loadingMore = false;
            }
        });
    }

    loadWatchlist(): void {
        if (this.currentUser?.username) {
            this.watchlistService.getWatchlistShows(this.currentUser.username).subscribe({
                next: (watchlist) => {
                    this.watchlist = watchlist;
                },
                error: (error) => {
                    console.error('Error loading watchlist:', error);
                }
            });
        }
    }

    addToWatchlist(show: TvShow): void {
        if (!this.currentUser?.username) return;

        if (this.currentUser.membership === 'FREE' && (this.watchlist?.content?.length || 0) >= 10) {
            if (confirm('You\'ve reached your watchlist limit of 10 shows. Upgrade to Premium for unlimited watchlist. Go to upgrade page?')) {
                this.goToUpgrade();
            }
            return;
        }

        this.addingToWatchlist = true;
        this.tvShowService.addToWatchlist(show.id, this.currentUser.username).subscribe({
            next: () => {
                alert('Added to watchlist!');
                this.loadWatchlist();
            },
            error: (error) => {
                alert('Error adding to watchlist: ' + (error.error?.message || 'Unknown error'));
            },
            complete: () => {
                this.addingToWatchlist = false;
            }
        });
    }

    removeFromWatchlist(show: TvShow): void {
        if (!this.currentUser?.username) return;

        if (confirm(`Remove "${show.name}" from your watchlist?`)) {
            this.watchlistService.removeFromWatchlist(this.currentUser.username, show.id).subscribe({
                next: () => {
                    alert('Removed from watchlist!');
                    this.loadWatchlist();
                },
                error: (error) => {
                    alert('Error removing from watchlist: ' + (error.error?.message || 'Endpoint not implemented yet'));
                }
            });
        }
    }

    goToUpgrade(): void {
        if (!this.currentUser?.username) {
            alert('User not found. Please log in again.');
            return;
        }

        if (confirm('Upgrade to Premium for unlimited watchlist and exclusive features?')) {
            this.upgradeService.upgradeUserToPremium(this.currentUser.username).subscribe({
                next: (userProfile) => {
                    if (userProfile.membership === 'PREMIUM') {
                        alert('Successfully upgraded to Premium! Refreshing your session...');
                        
                        // Refresh the user token to get updated membership info
                        this.authService.refreshUserToken().subscribe({
                            next: (updatedUser) => {
                                console.log('Token refreshed with new membership:', updatedUser);
                                // The UI will update automatically via currentUser$ subscription
                            },
                            error: (error) => {
                                console.error('Error refreshing token:', error);
                                alert('Upgrade successful but please refresh the page to see changes.');
                            }
                        });
                    } else {
                        alert('Upgrade failed - membership not updated.');
                    }
                },
                error: (error) => {
                    console.error('Error upgrading user:', error);
                    if (error.status === 400) {
                        alert('You already have Premium membership!');
                    } else {
                        alert('Upgrade failed. Please try again later.');
                    }
                }
            });
        }
    }


    onFilterChange(): void {
        // For dropdown changes (status, language) - immediate update
        this.currentPage = 0;
        this.loadAllShows();
    }

    onNameFilterChange(): void {
        // For text input (name) - debounced update
        this.searchSubject.next(this.searchFilter.name || '');
    }

    clearFilters(): void {
        this.searchFilter = {
            name: '',
            status: '',
            language: ''
        };
        this.currentPage = 0;
        this.loadAllShows();
    }


    goToShowDetails(showId: number): void {
        this.router.navigate(['/show', showId]);
    }

    logout(): void {
        this.authService.logout();
        // Navigation will happen automatically via currentUser$ subscription
    }

    goToAdmin(): void {
        this.router.navigate(['/admin']);
    }

    toggleUserDropdown(): void {
        this.showUserDropdown = !this.showUserDropdown;
    }

    closeUserDropdown(): void {
        this.showUserDropdown = false;
    }
}