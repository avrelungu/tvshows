export interface User {
    id: string;
    username: string;
    email: string;
    role: 'FREE' | 'PREMIUM' | 'ADMIN';
    token?: string;
}

export interface LoginUser extends User {
    token: string;
    refreshToken: string;
}

export interface UserProfile {
    id: string;
    username: string;
    email: string;
    memberType: 'FREE' | 'PREMIUM' | 'ADMIN';
    firstName: string;
    lastName: string;
}

export interface SignUpRequest {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    memberType: string;
    role: string;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface UpgradeProfileRequest {
    username: string;
    memberType: string;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}