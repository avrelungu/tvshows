-- schema.sql

CREATE TABLE IF NOT EXISTS reviews (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

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

CREATE INDEX IF NOT EXISTS idx_reviews_username ON reviews(username);
CREATE INDEX IF NOT EXISTS idx_reviews_tv_show_id ON reviews(tv_show_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews(created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_approved ON reviews(is_approved);
CREATE INDEX IF NOT EXISTS idx_reviews_flagged ON reviews(is_flagged);
