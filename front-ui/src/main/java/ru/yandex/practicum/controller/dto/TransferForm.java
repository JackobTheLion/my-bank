package ru.yandex.practicum.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferForm(
        @NotBlank(message = "Выберите получателя") @Size(max = 255) String recipientLogin,
        @NotNull(message = "Укажите сумму") @Positive(message = "Сумма должна быть больше нуля") Long amount
) {
}
