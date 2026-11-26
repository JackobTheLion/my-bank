package ru.yandex.practicum.mybank.commons.dto;

import java.time.LocalDate;

public record AccountResponse(
        String login,
        String name,
        LocalDate birthDate,
        long balance
) {
}
