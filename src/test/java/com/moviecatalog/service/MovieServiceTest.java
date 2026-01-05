package com.moviecatalog.service;

import com.moviecatalog.dto.MovieDTO;
import com.moviecatalog.entity.Director;
import com.moviecatalog.entity.Movie;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.DirectorRepository;
import com.moviecatalog.repository.GenreRepository;
import com.moviecatalog.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private DirectorRepository directorRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private MovieService movieService;

    private Director director;
    private Movie movie;
    private MovieDTO movieDTO;

    @BeforeEach
    void setUp() {
        director = new Director();
        director.setId(1L);
        director.setFirstName("Иван");
        director.setLastName("Иванов");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Тестовый фильм");
        movie.setDescription("Описание фильма");
        movie.setReleaseDate(LocalDate.of(2020, 1, 1));
        movie.setDuration(120);
        movie.setDirector(director);
        movie.setGenres(new HashSet<>());

        movieDTO = new MovieDTO();
        movieDTO.setTitle("Тестовый фильм");
        movieDTO.setDescription("Описание фильма");
        movieDTO.setReleaseDate(LocalDate.of(2020, 1, 1));
        movieDTO.setDuration(120);
        movieDTO.setDirectorId(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testGetAllMovies() {
        List<Movie> movies = Arrays.asList(movie);
        when(movieRepository.findAllWithRelations()).thenReturn(movies);

        List<MovieDTO> result = movieService.getAllMovies();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовый фильм", result.get(0).getTitle());
        verify(movieRepository).findAllWithRelations();
    }

    @Test
    @SuppressWarnings("null")
    void testGetMovieById_Success() {
        when(movieRepository.findByIdWithRelations(1L)).thenReturn(Optional.of(movie));

        MovieDTO result = movieService.getMovieById(1L);

        assertNotNull(result);
        assertEquals("Тестовый фильм", result.getTitle());
        assertEquals(120, result.getDuration());
        verify(movieRepository).findByIdWithRelations(1L);
    }

    @Test
    void testGetMovieById_NotFound() {
        when(movieRepository.findByIdWithRelations(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(1L));
        verify(movieRepository).findByIdWithRelations(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testCreateMovie_Success() {
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieDTO result = movieService.createMovie(movieDTO);

        assertNotNull(result);
        assertEquals("Тестовый фильм", result.getTitle());
        verify(directorRepository).findById(1L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @SuppressWarnings("null")
    void testCreateMovie_DirectorNotFound() {
        when(directorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.createMovie(movieDTO));
        verify(directorRepository).findById(1L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @SuppressWarnings("null")
    void testUpdateMovie_Success() {
        MovieDTO updateDTO = new MovieDTO();
        updateDTO.setTitle("Обновленный фильм");
        updateDTO.setDescription("Новое описание");
        updateDTO.setReleaseDate(LocalDate.of(2021, 1, 1));
        updateDTO.setDuration(150);
        updateDTO.setDirectorId(1L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(directorRepository.findById(1L)).thenReturn(Optional.of(director));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieDTO result = movieService.updateMovie(1L, updateDTO);

        assertNotNull(result);
        verify(movieRepository).findById(1L);
        verify(directorRepository).findById(1L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @SuppressWarnings("null")
    void testUpdateMovie_MovieNotFound() {
        MovieDTO updateDTO = new MovieDTO();
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.updateMovie(1L, updateDTO));
        verify(movieRepository).findById(1L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void testDeleteMovie_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        doNothing().when(movieRepository).deleteById(1L);

        movieService.deleteMovie(1L);

        verify(movieRepository).existsById(1L);
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void testDeleteMovie_NotFound() {
        when(movieRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> movieService.deleteMovie(1L));
        verify(movieRepository).existsById(1L);
        verify(movieRepository, never()).deleteById(anyLong());
    }
}

