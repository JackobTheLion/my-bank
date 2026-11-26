package ru.yandex.practicum.mybank.cash.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.security.CurrentUserLogin;

@RequestMapping("/cash")
public interface CashController {

    @PostMapping("/operations")
    @PreAuthorize("hasAuthority('SCOPE_cash:write')")
    MoneyOperationResponse applyCashOperation(
            @Valid @RequestBody CashOperationRequest request,
            @CurrentUserLogin String login
    );
}
