package ru.yandex.practicum.mybank.accounts.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.mybank.commons.dto.AccountsCashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

@RequestMapping("/internal/accounts")
public interface AccountOperationsController {

    @PostMapping("/cash-operations")
    @PreAuthorize("hasAuthority('SCOPE_accounts:cash')")
    MoneyOperationResponse applyCashOperation(@Valid @RequestBody AccountsCashOperationRequest request);

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority('SCOPE_accounts:transfer')")
    MoneyOperationResponse transfer(@Valid @RequestBody AccountsTransferRequest request);
}
