package ru.yandex.practicum.mybank.transfer.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;
import ru.yandex.practicum.mybank.commons.security.CurrentUserLogin;

@RequestMapping("/transfer")
public interface TransferController {

    @PostMapping("/operations")
    @PreAuthorize("hasAuthority('SCOPE_transfer:write')")
    MoneyOperationResponse transfer(
            @Valid @RequestBody TransferRequest request,
            @CurrentUserLogin String login
    );
}
