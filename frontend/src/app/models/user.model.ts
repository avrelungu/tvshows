export interface User {
    id: string;
    username: string;
    email: string;
    role: string;
    token?: string;
}

export interface LoginUser extends User {
    token: string;
    refreshToken: string;
    membership: string;
}

export interface UserProfile {
    id: string;
    username: string;
    email: string;
    membership: string;
    firstName: string;
    lastName: string;
}

export interface SignUpRequest {
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    password: string;
    role: string;
    membership: string;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface UpgradeProfileRequest {
    username: string;
    membership: string;
}

export interface RefreshTokenRequest {
    refreshToken: string;
}