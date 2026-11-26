package ru.yandex.practicum.mybank.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;
import ru.yandex.practicum.mybank.transfer.exception.TransferOperationException;
import ru.yandex.practicum.mybank.transfer.service.TransferService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final RestClient transferClient;

    @Override
    public MoneyOperationResponse transfer(String senderLogin, TransferRequest request) {
        MoneyOperationResponse result = applyTransfer(senderLogin, request);
        notifySender(senderLogin);
        return result;
    }

    private MoneyOperationResponse applyTransfer(String senderLogin, TransferRequest request) {
        AccountsTransferRequest body = new AccountsTransferRequest(senderLogin, request.recipientLogin(), request.amount());
        try {
            MoneyOperationResponse result = transferClient.post()
                    .uri("http://accounts/internal/accounts/transfers")
                    .body(body)
                    .retrieve()
                    .body(MoneyOperationResponse.class);
            if (result == null || result.balance() == null) {
                throw accountsUnavailable(null);
            }
            return result;
        } catch (RestClientResponseException failure) {
            throw switch (failure.getStatusCode().value()) {
                case 400 -> new TransferOperationException(HttpStatus.BAD_REQUEST, "Invalid transfer", failure);
                case 404 -> new TransferOperationException(HttpStatus.NOT_FOUND, "Account not found", failure);
                case 409 -> new TransferOperationException(HttpStatus.CONFLICT, "Insufficient funds", failure);
                default -> accountsUnavailable(failure);
            };
        } catch (RestClientException | OAuth2AuthorizationException | IllegalStateException failure) {
            throw accountsUnavailable(failure);
        }
    }

    private void notifySender(String senderLogin) {
        try {
            transferClient.post()
                    .uri("http://notifications/internal/notifications")
                    .body(new NotificationRequest(senderLogin, "TRANSFER_SENT"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException | OAuth2AuthorizationException | IllegalStateException failure) {
            log.warn("Transfer notification delivery failed ({})", failure.getClass().getSimpleName());
        }
    }

    private TransferOperationException accountsUnavailable(Throwable cause) {
        return new TransferOperationException(HttpStatus.SERVICE_UNAVAILABLE, "Accounts service is unavailable", cause);
    }
}
