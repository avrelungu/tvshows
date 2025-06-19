CREATE TABLE IF NOT EXISTS tv_shows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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
    -- Remove genres column from here for normalization
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

CREATE TABLE IF NOT EXISTS genres (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL
    );

CREATE TABLE IF NOT EXISTS tv_show_genres (
                                              tv_show_id UUID NOT NULL,
                                              genre_id UUID NOT NULL,
                                              PRIMARY KEY (tv_show_id, genre_id),
    FOREIGN KEY (tv_show_id) REFERENCES tv_shows(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);