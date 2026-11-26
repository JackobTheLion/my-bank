package ru.yandex.practicum.mybank.commons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AccountProfileRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull LocalDate birthDate
) {
}
