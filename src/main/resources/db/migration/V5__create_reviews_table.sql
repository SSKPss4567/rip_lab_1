CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    author_name VARCHAR(200) NOT NULL,
    comment VARCHAR(2000),
    rating INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    movie_id BIGINT NOT NULL,
    CONSTRAINT fk_reviews_movie 
        FOREIGN KEY (movie_id) 
        REFERENCES movies(id) 
        ON DELETE CASCADE,
    CONSTRAINT check_rating_range 
        CHECK (rating >= 1 AND rating <= 10)
);

CREATE INDEX idx_reviews_movie_id ON reviews(movie_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

