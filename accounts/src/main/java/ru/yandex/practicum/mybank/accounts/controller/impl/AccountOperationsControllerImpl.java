package ru.yandex.practicum.mybank.accounts.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.mybank.accounts.controller.AccountOperationsController;
import ru.yandex.practicum.mybank.accounts.service.MoneyOperationService;
import ru.yandex.practicum.mybank.commons.dto.AccountsCashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

@RestController
@RequiredArgsConstructor
public class AccountOperationsControllerImpl implements AccountOperationsController {

    private final MoneyOperationService moneyOperationService;

    @Override
    public MoneyOperationResponse applyCashOperation(AccountsCashOperationRequest request) {
        return moneyOperationService.applyCashOperation(request);
    }

    @Override
    public MoneyOperationResponse transfer(AccountsTransferRequest request) {
        return moneyOperationService.transfer(request);
    }
}
