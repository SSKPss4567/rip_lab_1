package com.moviecatalog.service;

import com.moviecatalog.dto.ReviewDTO;
import com.moviecatalog.entity.Movie;
import com.moviecatalog.entity.Review;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.MovieRepository;
import com.moviecatalog.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByMovieId(@NonNull Long movieId) {
        return reviewRepository.findByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(@NonNull Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв с id " + id + " не найден"));
        return convertToDTO(review);
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        Long movieId = Objects.requireNonNull(reviewDTO.getMovieId(), "Movie ID cannot be null");
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + movieId + " не найден"));

        Review review = new Review();
        review.setAuthorName(reviewDTO.getAuthorName());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setMovie(movie);

        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    @Transactional
    public ReviewDTO updateReview(@NonNull Long id, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Отзыв с id " + id + " не найден"));

        Long movieId = Objects.requireNonNull(reviewDTO.getMovieId(), "Movie ID cannot be null");
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Фильм с id " + movieId + " не найден"));

        review.setAuthorName(reviewDTO.getAuthorName());
        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setMovie(movie);

        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    @Transactional
    public void deleteReview(@NonNull Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Отзыв с id " + id + " не найден");
        }
        reviewRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingByMovieId(@NonNull Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Фильм с id " + movieId + " не найден");
        }
        Double averageRating = reviewRepository.findAverageRatingByMovieId(movieId);
        return averageRating != null ? averageRating : 0.0;
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setAuthorName(review.getAuthorName());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setMovieId(review.getMovie().getId());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}

