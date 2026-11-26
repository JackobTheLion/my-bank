package ru.yandex.practicum.mybank.commons.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRequest(
        @NotBlank String login,
        @NotBlank String type
) {
}
