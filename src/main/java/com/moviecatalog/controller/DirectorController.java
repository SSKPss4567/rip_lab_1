package com.moviecatalog.controller;

import com.moviecatalog.dto.DirectorDTO;
import com.moviecatalog.service.DirectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDTO>> getAllDirectors() {
        return ResponseEntity.ok(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDTO> getDirectorById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(directorService.getDirectorById(id));
    }

    @PostMapping
    public ResponseEntity<DirectorDTO> createDirector(@Valid @RequestBody DirectorDTO directorDTO) {
        DirectorDTO createdDirector = directorService.createDirector(directorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDirector);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DirectorDTO> updateDirector(@PathVariable @NonNull Long id, @Valid @RequestBody DirectorDTO directorDTO) {
        return ResponseEntity.ok(directorService.updateDirector(id, directorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable @NonNull Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}

