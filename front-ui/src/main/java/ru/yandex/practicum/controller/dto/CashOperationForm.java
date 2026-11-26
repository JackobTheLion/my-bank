package ru.yandex.practicum.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.mybank.commons.dto.CashAction;

public record CashOperationForm(
        @NotNull CashAction action,
        @NotNull(message = "Укажите сумму") @Positive(message = "Сумма должна быть больше нуля") Long amount
) {
}
