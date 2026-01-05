package com.moviecatalog.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long id;

    @NotBlank(message = "Имя автора обязательно")
    @Size(max = 200, message = "Имя автора не должно превышать 200 символов")
    private String authorName;

    @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
    private String comment;

    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть от 1 до 10")
    @Max(value = 10, message = "Рейтинг должен быть от 1 до 10")
    private Integer rating;

    @NotNull(message = "Фильм обязателен")
    private Long movieId;

    private LocalDateTime createdAt;
}

