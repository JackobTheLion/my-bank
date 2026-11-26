package ru.yandex.practicum.mybank.cash.service;

import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

public interface CashOperationService {

    MoneyOperationResponse applyCashOperation(String login, CashOperationRequest request);
}
