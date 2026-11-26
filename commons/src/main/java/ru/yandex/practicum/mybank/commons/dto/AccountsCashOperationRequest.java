package ru.yandex.practicum.mybank.commons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AccountsCashOperationRequest(
        @NotBlank @Size(max = 255) String login,
        @NotNull CashAction action,
        @Positive long amount
) {
}
