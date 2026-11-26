package ru.yandex.practicum.mybank.accounts.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.mybank.accounts.controller.AccountController;
import ru.yandex.practicum.mybank.accounts.service.AccountService;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController {

    private final AccountService accountService;

    @Override
    public AccountResponse getCurrentAccount(String login) {
        return accountService.getByLogin(login);
    }

    @Override
    public AccountResponse updateCurrentAccount(AccountProfileRequest request, String login) {
        return accountService.updateProfile(login, request);
    }

    @Override
    public List<AccountRecipientResponse> listRecipients(String login) {
        return accountService.listRecipients(login);
    }
}
