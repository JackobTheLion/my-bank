package ru.yandex.practicum.mybank.cash.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.mybank.cash.controller.CashController;
import ru.yandex.practicum.mybank.cash.service.CashOperationService;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

@RestController
@RequiredArgsConstructor
public class CashControllerImpl implements CashController {

    private final CashOperationService cashOperationService;

    @Override
    public MoneyOperationResponse applyCashOperation(CashOperationRequest request, String login) {
        return cashOperationService.applyCashOperation(login, request);
    }
}
