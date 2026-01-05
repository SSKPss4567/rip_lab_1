package com.moviecatalog.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {

    private Long id;

    @NotBlank(message = "Название фильма обязательно")
    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String title;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    @NotNull(message = "Дата выпуска обязательна")
    @PastOrPresent(message = "Дата выпуска не может быть в будущем")
    private LocalDate releaseDate;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть больше 0")
    private Integer duration;

    @NotNull(message = "Режиссер обязателен")
    private Long directorId;

    private Set<Long> genreIds;

    private Double averageRating;
}

