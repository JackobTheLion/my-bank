package ru.yandex.practicum.service;

import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;

import java.util.List;

public interface AccountsClient {

    AccountResponse getCurrentAccount();

    void updateProfile(AccountProfileRequest profile);

    List<AccountRecipientResponse> listRecipients();
}
