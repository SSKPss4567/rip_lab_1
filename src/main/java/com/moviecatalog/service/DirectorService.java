package com.moviecatalog.service;

import com.moviecatalog.dto.DirectorDTO;
import com.moviecatalog.entity.Director;
import com.moviecatalog.exception.ResourceNotFoundException;
import com.moviecatalog.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    @Transactional(readOnly = true)
    public List<DirectorDTO> getAllDirectors() {
        return directorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DirectorDTO getDirectorById(@NonNull Long id) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер с id " + id + " не найден"));
        return convertToDTO(director);
    }

    @Transactional
    public DirectorDTO createDirector(DirectorDTO directorDTO) {
        Director director = new Director();
        director.setFirstName(directorDTO.getFirstName());
        director.setLastName(directorDTO.getLastName());
        director.setBirthDate(directorDTO.getBirthDate());
        director.setBiography(directorDTO.getBiography());

        Director savedDirector = directorRepository.save(director);
        return convertToDTO(savedDirector);
    }

    @Transactional
    public DirectorDTO updateDirector(@NonNull Long id, DirectorDTO directorDTO) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Режиссер с id " + id + " не найден"));

        director.setFirstName(directorDTO.getFirstName());
        director.setLastName(directorDTO.getLastName());
        director.setBirthDate(directorDTO.getBirthDate());
        director.setBiography(directorDTO.getBiography());

        Director updatedDirector = directorRepository.save(director);
        return convertToDTO(updatedDirector);
    }

    @Transactional
    public void deleteDirector(@NonNull Long id) {
        if (!directorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Режиссер с id " + id + " не найден");
        }
        directorRepository.deleteById(id);
    }

    private DirectorDTO convertToDTO(Director director) {
        DirectorDTO dto = new DirectorDTO();
        dto.setId(director.getId());
        dto.setFirstName(director.getFirstName());
        dto.setLastName(director.getLastName());
        dto.setBirthDate(director.getBirthDate());
        dto.setBiography(director.getBiography());
        return dto;
    }
}

