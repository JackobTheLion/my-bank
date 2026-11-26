package ru.yandex.practicum.mybank.accounts.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;
import ru.yandex.practicum.mybank.commons.security.CurrentUserLogin;

import java.util.List;

@RequestMapping("/accounts")
public interface AccountController {

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_accounts:read')")
    AccountResponse getCurrentAccount(@CurrentUserLogin String login);

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_accounts:write')")
    AccountResponse updateCurrentAccount(
            @Valid @RequestBody AccountProfileRequest request,
            @CurrentUserLogin String login
    );

    @GetMapping("/recipients")
    @PreAuthorize("hasAuthority('SCOPE_accounts:read')")
    List<AccountRecipientResponse> listRecipients(@CurrentUserLogin String login);
}
