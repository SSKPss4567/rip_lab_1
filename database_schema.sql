DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS movie_genres CASCADE;
DROP TABLE IF EXISTS movies CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS directors CASCADE;

-- Create directors table
CREATE TABLE directors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    biography VARCHAR(500)
);

-- Create genres table
CREATE TABLE genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Create movies table
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

-- Create movie_genres junction table (Many-to-Many relationship)
CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT pk_movie_genres 
        PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_movie_genres_movie 
        FOREIGN KEY (movie_id) 
        REFERENCES movies(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_movie_genres_genre 
        FOREIGN KEY (genre_id) 
        REFERENCES genres(id) 
        ON DELETE CASCADE
);

-- Create reviews table
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

-- Create indexes for better performance
CREATE INDEX idx_movies_director_id ON movies(director_id);
CREATE INDEX idx_movie_genres_movie_id ON movie_genres(movie_id);
CREATE INDEX idx_movie_genres_genre_id ON movie_genres(genre_id);
CREATE INDEX idx_reviews_movie_id ON reviews(movie_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Insert test data

INSERT INTO directors (first_name, last_name, birth_date, biography) VALUES
('Christopher', 'Nolan', '1970-07-30', 'British-American filmmaker known for his complex narratives and practical effects'),
('Quentin', 'Tarantino', '1963-03-27', 'American filmmaker known for nonlinear storylines and stylized violence'),
('Martin', 'Scorsese', '1942-11-17', 'American filmmaker known for his work in crime and drama films'),
('Steven', 'Spielberg', '1946-12-18', 'American filmmaker and producer, one of the pioneers of the New Hollywood era');

INSERT INTO genres (name) VALUES
('Action'),
('Drama'),
('Sci-Fi'),
('Thriller'),
('Crime'),
('Adventure'),
('Comedy'),
('Horror');

INSERT INTO movies (title, description, release_date, duration, director_id) VALUES
('Inception', 'A skilled thief is given a chance at redemption if he can perform the impossible task of inception: planting an idea in someone''s mind.', '2010-07-16', 148, 1),
('The Dark Knight', 'Batman faces the Joker, a criminal mastermind who seeks to undermine Batman''s influence and create chaos in Gotham City.', '2008-07-18', 152, 1),
('Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.', '1994-10-14', 154, 2),
('Django Unchained', 'A freed slave teams up with a German bounty hunter to rescue his wife from a brutal Mississippi plantation owner.', '2012-12-25', 165, 2),
('The Departed', 'An undercover cop and a mole in the police attempt to identify each other while infiltrating an Irish gang in South Boston.', '2006-10-06', 151, 3),
('Goodfellas', 'The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners.', '1990-09-21', 146, 3),
('Jurassic Park', 'A pragmatic paleontologist visiting an almost complete theme park is tasked with protecting a couple of kids after a power failure causes the park''s cloned dinosaurs to run loose.', '1993-06-11', 127, 4),
('Saving Private Ryan', 'Following the Normandy Landings, a group of U.S. soldiers go behind enemy lines to retrieve a paratrooper whose brothers have been killed in action.', '1998-07-24', 169, 4);

-- Insert movie_genres (Many-to-Many relationship)
INSERT INTO movie_genres (movie_id, genre_id) VALUES
(1, 3), -- Inception - Sci-Fi
(1, 2), -- Inception - Drama
(1, 4), -- Inception - Thriller
(2, 1), -- The Dark Knight - Action
(2, 2), -- The Dark Knight - Drama
(2, 5), -- The Dark Knight - Crime
(3, 5), -- Pulp Fiction - Crime
(3, 2), -- Pulp Fiction - Drama
(3, 7), -- Pulp Fiction - Comedy
(4, 1), -- Django Unchained - Action
(4, 2), -- Django Unchained - Drama
(4, 5), -- Django Unchained - Crime
(5, 2), -- The Departed - Drama
(5, 5), -- The Departed - Crime
(5, 4), -- The Departed - Thriller
(6, 2), -- Goodfellas - Drama
(6, 5), -- Goodfellas - Crime
(7, 1), -- Jurassic Park - Action
(7, 6), -- Jurassic Park - Adventure
(7, 3), -- Jurassic Park - Sci-Fi
(8, 1), -- Saving Private Ryan - Action
(8, 2), -- Saving Private Ryan - Drama
(8, 6); -- Saving Private Ryan - Adventure

INSERT INTO reviews (author_name, comment, rating, created_at, movie_id) VALUES
('John Doe', 'Mind-bending masterpiece! The concept of shared dreaming is brilliantly executed.', 9, '2024-01-15 10:30:00', 1),
('Jane Smith', 'Confusing at times but visually stunning. Christopher Nolan at his best.', 8, '2024-01-20 14:22:00', 1),
('Mike Johnson', 'One of the best superhero movies ever made. Heath Ledger''s performance is legendary.', 10, '2024-02-01 09:15:00', 2),
('Sarah Williams', 'Dark, intense, and perfectly crafted. A true cinematic achievement.', 9, '2024-02-05 16:45:00', 2),
('Tom Brown', 'Tarantino''s best work. The dialogue is sharp and the story is captivating.', 10, '2024-02-10 11:20:00', 3),
('Emily Davis', 'A classic that never gets old. The non-linear narrative is genius.', 9, '2024-02-12 13:30:00', 3),
('Robert Wilson', 'Powerful performances and great storytelling. A modern western classic.', 8, '2024-02-15 15:00:00', 4),
('Lisa Anderson', 'Intense and gripping. The tension between characters is palpable.', 9, '2024-02-18 10:45:00', 5),
('David Martinez', 'Scorsese''s masterpiece. The direction and acting are flawless.', 10, '2024-02-20 12:00:00', 6),
('Jennifer Lee', 'Groundbreaking special effects for its time. Still holds up today.', 8, '2024-02-22 14:30:00', 7),
('Chris Taylor', 'Emotional and powerful. One of Spielberg''s finest works.', 9, '2024-02-25 16:15:00', 8),
('Amanda White', 'The opening scene is one of the most intense in cinema history.', 10, '2024-02-28 09:30:00', 8);
