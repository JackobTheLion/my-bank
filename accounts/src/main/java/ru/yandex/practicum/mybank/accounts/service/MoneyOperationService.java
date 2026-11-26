package ru.yandex.practicum.mybank.accounts.service;

import ru.yandex.practicum.mybank.commons.dto.AccountsCashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

public interface MoneyOperationService {

    MoneyOperationResponse applyCashOperation(AccountsCashOperationRequest request);

    MoneyOperationResponse transfer(AccountsTransferRequest request);
}
