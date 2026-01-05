CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL,
    director_id BIGINT NOT NULL,
    CONSTRAINT fk_movies_director 
        FOREIGN KEY (director_id) 
        REFERENCES directors(id)
);

CREATE INDEX idx_movies_director_id ON movies(director_id);

