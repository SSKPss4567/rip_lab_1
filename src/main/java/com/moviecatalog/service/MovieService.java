package com.moviecatalog.service;

import com.moviecatalog.dto.MovieDTO;
import com.moviecatalog.entity.Director;
import com.moviecatalog.entity.Genre;
import com.moviecatalog.entity.Movie;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.DirectorRepository;
import com.moviecatalog.repository.GenreRepository;
import com.moviecatalog.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAllWithRelations().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieDTO getMovieById(@NonNull Long id) {
        Movie movie = movieRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + id + " не найден"));
        return convertToDTO(movie);
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Long directorId = Objects.requireNonNull(movieDTO.getDirectorId(), "Director ID cannot be null");
        Director director = directorRepository.findById(directorId)
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер с id " + directorId + " не найден"));

        Movie movie = new Movie();
        movie.setTitle(movieDTO.getTitle());
        movie.setDescription(movieDTO.getDescription());
        movie.setReleaseDate(movieDTO.getReleaseDate());
        movie.setDuration(movieDTO.getDuration());
        movie.setDirector(director);

        if (movieDTO.getGenreIds() != null && !movieDTO.getGenreIds().isEmpty()) {
            Set<Genre> genres = movieDTO.getGenreIds().stream()
                    .map((@NonNull Long genreId) -> genreRepository.findById(genreId)
                            .orElseThrow(() -> new ResourceNotFoundException("Жанр с id " + genreId + " не найден")))
                    .collect(Collectors.toSet());
            movie.setGenres(genres);
        }

        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    @Transactional
    public MovieDTO updateMovie(@NonNull Long id, MovieDTO movieDTO) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + id + " не найден"));

        Long directorId = Objects.requireNonNull(movieDTO.getDirectorId(), "Director ID cannot be null");
        Director director = directorRepository.findById(directorId)
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер с id " + directorId + " не найден"));

        movie.setTitle(movieDTO.getTitle());
        movie.setDescription(movieDTO.getDescription());
        movie.setReleaseDate(movieDTO.getReleaseDate());
        movie.setDuration(movieDTO.getDuration());
        movie.setDirector(director);

        if (movieDTO.getGenreIds() != null) {
            Set<Genre> genres = movieDTO.getGenreIds().stream()
                    .map((@NonNull Long genreId) -> genreRepository.findById(genreId)
                            .orElseThrow(() -> new ResourceNotFoundException("Жанр с id " + genreId + " не найден")))
                    .collect(Collectors.toSet());
            movie.setGenres(genres);
        }

        Movie updatedMovie = movieRepository.save(movie);
        return convertToDTO(updatedMovie);
    }

    @Transactional
    public void deleteMovie(@NonNull Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Фильм с id " + id + " не найден");
        }
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getRecommendedMovies(@NonNull Long movieId) {
        Movie movie = movieRepository.findByIdWithRelations(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + movieId + " не найден"));

        Set<Long> genreIds = movie.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        if (genreIds.isEmpty()) {
            return List.of();
        }

        List<Movie> similarMovies = movieRepository.findSimilarMovies(movieId, genreIds);

        return similarMovies.stream()
                .sorted((a, b) -> {
                    Double ratingA = a.getAverageRating();
                    Double ratingB = b.getAverageRating();
                    int ratingCompare = Double.compare(ratingB != null ? ratingB : 0.0, ratingA != null ? ratingA : 0.0);
                    if (ratingCompare != 0) {
                        return ratingCompare;
                    }
                    return b.getReleaseDate().compareTo(a.getReleaseDate());
                })
                .limit(5)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setDuration(movie.getDuration());
        dto.setDirectorId(movie.getDirector().getId());
        dto.setGenreIds(movie.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet()));
        dto.setAverageRating(movie.getAverageRating());
        return dto;
    }
}

