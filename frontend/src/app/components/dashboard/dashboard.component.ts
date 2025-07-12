import { Component, OnInit } from '@angular/core';
import { TvShowService } from '../../services/tvshow.service';
import { WatchlistService } from '../../services/watchlist.service';
import { AuthService } from '../../services/auth.service';
import { TvShow, PageResponse } from '../../models/tvshow.model';
import { WatchlistItem } from '../../models/watchlist.model';
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
    selector: 'app-dashboard',
    template: `
        <div class="dashboard">
            <header class="dashboard-header">
                <h1>TV Shows Dashboard</h1>
                <div class="user-info" *ngIf="currentUser">
                    <span>Welcome, {{ currentUser.username }}!</span>
                    <span class="member-type">{{ currentUser.role }}</span>
                    <button (click)="logout()">Logout</button>
                </div>
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
                        <div class="show-card" *ngFor="let show of topRatedShows.content">
                            <img [src]="show.imageMedium" [alt]="show.name"/>
                            <div class="show-info">
                                <h3>{{ show.name }}</h3>
                                <p class="rating">⭐ {{ show.rating }}/10</p>
                                <p class="genres">{{ show.genres.join(', ') || 'No genres' }}</p>
                                <button (click)="addToWatchlist(show)" [disabled]="addingToWatchlist">
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
                        <input
                                type="text"
                                placeholder="Search shows..."
                                [(ngModel)]="searchFilter.name"
                                (input)="onFilterChange()">

                        <select [(ngModel)]="searchFilter.status" (change)="onFilterChange()">
                            <option value="">All Status</option>
                            <option value="Running">Running</option>
                            <option value="Ended">Ended</option>
                        </select>

                        <select [(ngModel)]="searchFilter.language" (change)="onFilterChange()">
                            <option value="">All Languages</option>
                            <option value="English">English</option>
                            <option value="Spanish">Spanish</option>
                            <option value="French">French</option>
                        </select>

                        <button (click)="clearFilters()">Clear Filters</button>
                    </div>

                    <div class="shows-grid" *ngIf="allShows && allShows.content">
                        <div class="show-card" *ngFor="let show of allShows.content">
                            <img [src]="show.imageMedium" [alt]="show.name"/>
                            <div class="show-info">
                                <h3>{{ show.name }}</h3>
                                <p class="rating">⭐ {{ show.rating }}/10</p>
                                <p class="status">{{ show.status }}</p>
                                <p class="genres">{{ show.genres.join(', ') || 'No genres' }}</p>
                                <button (click)="addToWatchlist(show)" [disabled]="addingToWatchlist">
                                    Add to Watchlist
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Pagination -->
                    <div class="pagination" *ngIf="allShows && allShows.totalPages">
                        <button
                                [disabled]="currentPage === 0"
                                (click)="changePage(currentPage - 1)">
                            Previous
                        </button>
                        <span>Page {{ currentPage + 1 }} of {{ allShows.totalPages }}</span>
                        <button
                                [disabled]="currentPage >= allShows.totalPages - 1"
                                (click)="changePage(currentPage + 1)">
                            Next
                        </button>
                    </div>
                </section>

                <!-- Watchlist Section -->
                <section *ngIf="activeSection === 'watchlist'">
                    <div class="section-header">
                        <h2>My Watchlist</h2>
                        <div class="watchlist-info" *ngIf="currentUser?.role === 'FREE'">
                            <span class="watchlist-limit">{{ watchlist.length || 0 }}/10 shows</span>
                            <button class="upgrade-btn" (click)="goToUpgrade()">
                                Upgrade for Unlimited
                            </button>
                        </div>
                    </div>

                    <!-- Upgrade Notice for FREE users approaching limit -->
                    <div class="upgrade-notice" *ngIf="currentUser?.role === 'FREE' && (watchlist.length || 0) >= 8">
                        <div class="notice-content">
                            <h3>Almost at your limit!</h3>
                            <p>You have {{ 10 - (watchlist.length || 0) }} watchlist slots remaining.</p>
                            <button (click)="goToUpgrade()" class="upgrade-cta">
                                Upgrade to Premium for unlimited watchlist
                            </button>
                        </div>
                    </div>

                    <div class="watchlist-grid" *ngIf="watchlist && watchlist.length > 0">
                        <div class="watchlist-item" *ngFor="let item of watchlist">
                            <img [src]="item.imageMedium" [alt]="item.name" />
                            <div class="item-info">
                                <h3>{{ item.name }}</h3>
                                <p>{{ item.description }}</p>
                            </div>
                        </div>
                    </div>
                    <div *ngIf="watchlist && watchlist.length === 0" class="empty-state">
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
        }

        .member-type {
            background: #667eea;
            color: white;
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.875rem;
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

        .watchlist-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
            gap: 1rem;
        }

        .watchlist-item {
            background: white;
            border-radius: 8px;
            padding: 1rem;
            display: flex;
            gap: 1rem;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .watchlist-item img {
            width: 80px;
            height: 120px;
            object-fit: cover;
            border-radius: 4px;
        }

        .item-info h3 {
            margin: 0 0 0.5rem 0;
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 1rem;
            margin-top: 2rem;
        }

        .pagination button {
            padding: 0.5rem 1rem;
            border: 1px solid #ddd;
            background: white;
            cursor: pointer;
            border-radius: 4px;
        }

        .pagination button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
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
export class DashboardComponent implements OnInit {
    currentUser: any;
    activeSection = 'top-rated';
    sections = [
        { key: 'top-rated', label: 'Top Rated' },
        { key: 'browse', label: 'Browse Shows' },
        { key: 'watchlist', label: 'My Watchlist' }
    ];

    topRatedShows: PageResponse<TvShow> | null = null;
    allShows: PageResponse<TvShow> | null = null;
    watchlist: WatchlistItem[] = [];

    currentPage = 0;
    pageSize = 12;
    addingToWatchlist = false;

    searchFilter = {
        name: '',
        status: '',
        language: ''
    };

    constructor(
        private tvShowService: TvShowService,
        private watchlistService: WatchlistService,
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        this.loadTopRatedShows();
        this.loadAllShows();
        this.loadWatchlist();
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
        this.tvShowService.getTvShows(this.currentPage, this.pageSize, this.searchFilter).subscribe({
            next: (shows) => {
                this.allShows = shows;
            },
            error: (error) => {
                console.error('Error loading shows:', error);
            }
        });
    }

    loadWatchlist(): void {
        if (this.currentUser?.username) {
            this.watchlistService.getWatchlist(this.currentUser.username).subscribe({
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

        // Check FREE user limit
        if (this.currentUser.role === 'FREE' && (this.watchlist?.length || 0) >= 10) {
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

    goToUpgrade(): void {
        this.router.navigate(['/upgrade']);
    }


    onFilterChange(): void {
        this.currentPage = 0;
        this.loadAllShows();
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

    changePage(page: number): void {
        this.currentPage = page;
        this.loadAllShows();
    }

    logout(): void {
        this.authService.logout();
    }
}