package ru.yandex.practicum.mybank.commons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferRequest(
        @NotBlank @Size(max = 255) String recipientLogin,
        @Positive long amount
) {
}
