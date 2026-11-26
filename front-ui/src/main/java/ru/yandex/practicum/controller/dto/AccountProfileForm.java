package ru.yandex.practicum.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AccountProfileForm(
        @NotBlank(message = "Укажите имя")
        @Size(max = 255, message = "Имя должно содержать не более 255 символов")
        String name,

        @NotNull(message = "Укажите дату рождения")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDate
) {
}
