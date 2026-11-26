package ru.yandex.practicum.service;

import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;

public interface MoneyOperationsClient {

    void applyCashOperation(CashOperationRequest operation);

    void transfer(TransferRequest transfer);
}
