package ru.yandex.practicum.mybank.accounts.service;

import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse getByLogin(String login);

    AccountResponse updateProfile(String login, AccountProfileRequest request);

    List<AccountRecipientResponse> listRecipients(String login);
}
