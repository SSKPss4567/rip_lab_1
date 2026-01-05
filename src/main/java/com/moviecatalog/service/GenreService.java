package com.moviecatalog.service;

import com.moviecatalog.dto.GenreDTO;
import com.moviecatalog.entity.Genre;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenreDTO getGenreById(@NonNull Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр с id " + id + " не найден"));
        return convertToDTO(genre);
    }

    @Transactional
    public GenreDTO createGenre(GenreDTO genreDTO) {
        Genre genre = new Genre();
        genre.setName(genreDTO.getName());

        Genre savedGenre = genreRepository.save(genre);
        return convertToDTO(savedGenre);
    }

    @Transactional
    public GenreDTO updateGenre(@NonNull Long id, GenreDTO genreDTO) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Жанр с id " + id + " не найден"));

        genre.setName(genreDTO.getName());

        Genre updatedGenre = genreRepository.save(genre);
        return convertToDTO(updatedGenre);
    }

    @Transactional
    public void deleteGenre(@NonNull Long id) {
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Жанр с id " + id + " не найден");
        }
        genreRepository.deleteById(id);
    }

    private GenreDTO convertToDTO(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }
}

