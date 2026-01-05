package com.moviecatalog.repository;

import com.moviecatalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTitle(String title);

    List<Movie> findByDirectorId(Long directorId);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.director LEFT JOIN FETCH m.genres")
    List<Movie> findAllWithRelations();

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.director LEFT JOIN FETCH m.genres WHERE m.id = :id")
    Optional<Movie> findByIdWithRelations(Long id);

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.director LEFT JOIN FETCH m.genres " +
           "WHERE m.id != :movieId AND EXISTS " +
           "(SELECT 1 FROM m.genres g WHERE g.id IN :genreIds)")
    List<Movie> findSimilarMovies(Long movieId, Set<Long> genreIds);
}

