package com.moviecatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO {

    private Long id;

    @NotBlank(message = "Название жанра обязательно")
    @Size(max = 100, message = "Название жанра не должно превышать 100 символов")
    private String name;
}

