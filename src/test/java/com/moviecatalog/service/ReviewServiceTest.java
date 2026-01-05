package com.moviecatalog.service;

import com.moviecatalog.dto.ReviewDTO;
import com.moviecatalog.entity.Movie;
import com.moviecatalog.entity.Review;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Movie movie;
    private Review review;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Тестовый фильм");
        movie.setReleaseDate(LocalDate.of(2020, 1, 1));

        review = new Review();
        review.setId(1L);
        review.setAuthorName("Иван Иванов");
        review.setComment("Отличный фильм!");
        review.setRating(9);
        review.setMovie(movie);
        review.setCreatedAt(LocalDateTime.now());

        reviewDTO = new ReviewDTO();
        reviewDTO.setAuthorName("Иван Иванов");
        reviewDTO.setComment("Отличный фильм!");
        reviewDTO.setRating(9);
        reviewDTO.setMovieId(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testCreateReview_Success() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDTO result = reviewService.createReview(reviewDTO);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getAuthorName());
        assertEquals(9, result.getRating());
        verify(movieRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @SuppressWarnings("null")
    void testCreateReview_MovieNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.createReview(reviewDTO));
        verify(movieRepository).findById(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @SuppressWarnings("null")
    void testGetAllReviews() {
        List<Review> reviews = Arrays.asList(review);
        when(reviewRepository.findAll()).thenReturn(reviews);

        List<ReviewDTO> result = reviewService.getAllReviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Иван Иванов", result.get(0).getAuthorName());
        verify(reviewRepository).findAll();
    }

    @Test
    @SuppressWarnings("null")
    void testGetReviewsByMovieId() {
        List<Review> reviews = Arrays.asList(review);
        when(reviewRepository.findByMovieId(1L)).thenReturn(reviews);

        List<ReviewDTO> result = reviewService.getReviewsByMovieId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getMovieId());
        verify(reviewRepository).findByMovieId(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testGetReviewById_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewDTO result = reviewService.getReviewById(1L);

        assertNotNull(result);
        assertEquals("Иван Иванов", result.getAuthorName());
        verify(reviewRepository).findById(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(1L));
        verify(reviewRepository).findById(1L);
    }

    @Test
    @SuppressWarnings("null")
    void testUpdateReview_Success() {
        ReviewDTO updateDTO = new ReviewDTO();
        updateDTO.setAuthorName("Петр Петров");
        updateDTO.setComment("Хороший фильм");
        updateDTO.setRating(8);
        updateDTO.setMovieId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDTO result = reviewService.updateReview(1L, updateDTO);

        assertNotNull(result);
        verify(reviewRepository).findById(1L);
        verify(movieRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testDeleteReview_Success() {
        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void testDeleteReview_NotFound() {
        when(reviewRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(1L));
        verify(reviewRepository).existsById(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAverageRatingByMovieId_Success() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAverageRatingByMovieId(1L)).thenReturn(8.5);

        Double result = reviewService.getAverageRatingByMovieId(1L);

        assertNotNull(result);
        assertEquals(8.5, result);
        verify(movieRepository).existsById(1L);
        verify(reviewRepository).findAverageRatingByMovieId(1L);
    }

    @Test
    void testGetAverageRatingByMovieId_NoReviews() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAverageRatingByMovieId(1L)).thenReturn(null);

        Double result = reviewService.getAverageRatingByMovieId(1L);

        assertNotNull(result);
        assertEquals(0.0, result);
        verify(movieRepository).existsById(1L);
        verify(reviewRepository).findAverageRatingByMovieId(1L);
    }

    @Test
    void testGetAverageRatingByMovieId_MovieNotFound() {
        when(movieRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getAverageRatingByMovieId(1L));
        verify(movieRepository).existsById(1L);
        verify(reviewRepository, never()).findAverageRatingByMovieId(anyLong());
    }
}

