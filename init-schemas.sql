-- Create schemas for each microservice
CREATE SCHEMA IF NOT EXISTS auth_service;
CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS shows_service;
CREATE SCHEMA IF NOT EXISTS review_service;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set search path to include all schemas
SET search_path TO auth_service, user_service, shows_service, review_service, public;

-- Auth Service Schema
CREATE TABLE IF NOT EXISTS auth_service.app_user (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    membership VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(20) DEFAULT 'LOCAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS auth_service.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth_service.app_user(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Service Schema
CREATE TABLE IF NOT EXISTS user_service.user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    member_type VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_service.watchlist (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES user_service.user_profiles(id) ON DELETE CASCADE,
    show_id INTEGER NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, show_id)
);

CREATE TABLE IF NOT EXISTS user_service.tv_show_search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES user_service.user_profiles(id) ON DELETE CASCADE,
    endpoint TEXT,
    filters JSONB,
    search_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shows Service Schema
CREATE TABLE IF NOT EXISTS shows_service.tv_shows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tv_show_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    language VARCHAR(50),
    status VARCHAR(50),
    runtime INT,
    average_runtime INT,
    premiered DATE,
    ended DATE,
    official_site VARCHAR(255),
    rating DECIMAL(3,1),
    schedule_time VARCHAR(10),
    schedule_days TEXT[],
    tvrage INT,
    thetvdb INT,
    imdb VARCHAR(50),
    image_medium VARCHAR(255),
    image_original VARCHAR(255),
    summary TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shows_service.genres (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS shows_service.tv_show_genres (
    tv_show_id UUID NOT NULL,
    genre_id UUID NOT NULL,
    PRIMARY KEY (tv_show_id, genre_id),
    FOREIGN KEY (tv_show_id) REFERENCES shows_service.tv_shows(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES shows_service.genres(id) ON DELETE CASCADE
);

-- Review Service Schema
CREATE TABLE IF NOT EXISTS review_service.reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL,
    tv_show_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content TEXT NOT NULL,
    is_approved BOOLEAN NOT NULL DEFAULT true,
    is_flagged BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_show_review UNIQUE (username, tv_show_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_auth_user_email ON auth_service.app_user(email);
CREATE INDEX IF NOT EXISTS idx_auth_user_username ON auth_service.app_user(username);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_user_id ON auth_service.refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_expires_at ON auth_service.refresh_tokens(expires_at);

CREATE INDEX IF NOT EXISTS idx_user_profiles_username ON user_service.user_profiles(username);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_service.user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_watchlist_user_id ON user_service.watchlist(user_id);
CREATE INDEX IF NOT EXISTS idx_watchlist_show_id ON user_service.watchlist(show_id);

CREATE INDEX IF NOT EXISTS idx_tv_shows_tv_show_id ON shows_service.tv_shows(tv_show_id);
CREATE INDEX IF NOT EXISTS idx_tv_shows_name ON shows_service.tv_shows(name);
CREATE INDEX IF NOT EXISTS idx_tv_show_genres_tv_show_id ON shows_service.tv_show_genres(tv_show_id);
CREATE INDEX IF NOT EXISTS idx_tv_show_genres_genre_id ON shows_service.tv_show_genres(genre_id);

CREATE INDEX IF NOT EXISTS idx_reviews_username ON review_service.reviews(username);
CREATE INDEX IF NOT EXISTS idx_reviews_tv_show_id ON review_service.reviews(tv_show_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON review_service.reviews(created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_approved ON review_service.reviews(is_approved);
CREATE INDEX IF NOT EXISTS idx_reviews_flagged ON review_service.reviews(is_flagged);

-- Grant permissions (adjust as needed for your security requirements)
GRANT ALL PRIVILEGES ON SCHEMA auth_service TO admin;
GRANT ALL PRIVILEGES ON SCHEMA user_service TO admin;
GRANT ALL PRIVILEGES ON SCHEMA shows_service TO admin;
GRANT ALL PRIVILEGES ON SCHEMA review_service TO admin;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth_service TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA user_service TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA shows_service TO admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA review_service TO admin;

GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA auth_service TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA user_service TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA shows_service TO admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA review_service TO admin;
