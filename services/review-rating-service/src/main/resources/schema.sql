-- schema.sql

CREATE TABLE IF NOT EXISTS reviews (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

     username VARCHAR(50) NOT NULL,

     tv_show_id BIGINT NOT NULL,

     rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),

     content TEXT NOT NULL,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reviews_username ON reviews(username);
CREATE INDEX IF NOT EXISTS idx_reviews_tv_show_id ON reviews(tv_show_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews(created_at);
