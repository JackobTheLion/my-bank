package ru.yandex.practicum.mybank.transfer.service;

import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;

public interface TransferService {

    MoneyOperationResponse transfer(String senderLogin, TransferRequest request);
}
