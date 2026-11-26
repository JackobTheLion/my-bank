package ru.yandex.practicum.mybank.transfer.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;
import ru.yandex.practicum.mybank.transfer.controller.TransferController;
import ru.yandex.practicum.mybank.transfer.service.TransferService;

@RestController
@RequiredArgsConstructor
public class TransferControllerImpl implements TransferController {

    private final TransferService transferService;

    @Override
    public MoneyOperationResponse transfer(TransferRequest request, String login) {
        return transferService.transfer(login, request);
    }
}
