package ru.yandex.practicum.mybank.cash.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.mybank.cash.exception.CashOperationException;
import ru.yandex.practicum.mybank.cash.service.CashOperationService;
import ru.yandex.practicum.mybank.commons.dto.AccountsCashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.CashAction;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashOperationServiceImpl implements CashOperationService {

    private final RestClient cashClient;

    @Override
    public MoneyOperationResponse applyCashOperation(String login, CashOperationRequest request) {
        MoneyOperationResponse response = updateBalance(login, request);
        sendNotification(login, request.action());
        return response;
    }

    private MoneyOperationResponse updateBalance(String login, CashOperationRequest request) {
        try {
            MoneyOperationResponse response = cashClient.post()
                    .uri("http://accounts/internal/accounts/cash-operations")
                    .body(new AccountsCashOperationRequest(login, request.action(), request.amount()))
                    .retrieve()
                    .body(MoneyOperationResponse.class);
            if (response == null || response.balance() == null) {
                throw accountsUnavailable(null);
            }
            return response;
        } catch (RestClientResponseException exception) {
            throw translateAccountsError(exception);
        } catch (IllegalStateException | OAuth2AuthorizationException | RestClientException exception) {
            throw accountsUnavailable(exception);
        }
    }

    private void sendNotification(String login, CashAction action) {
        String type = switch (action) {
            case DEPOSIT -> "CASH_DEPOSITED";
            case WITHDRAW -> "CASH_WITHDRAWN";
        };
        try {
            cashClient.post()
                    .uri("http://notifications/internal/notifications")
                    .body(new NotificationRequest(login, type))
                    .retrieve()
                    .toBodilessEntity();
        } catch (IllegalStateException | OAuth2AuthorizationException | RestClientException exception) {
            log.warn("Cash notification {} delivery failed ({})", type, exception.getClass().getSimpleName());
        }
    }

    private CashOperationException translateAccountsError(RestClientResponseException exception) {
        return switch (exception.getStatusCode().value()) {
            case 400 -> new CashOperationException(HttpStatus.BAD_REQUEST, "Invalid cash operation", exception);
            case 404 -> new CashOperationException(HttpStatus.NOT_FOUND, "Account not found", exception);
            case 409 -> new CashOperationException(HttpStatus.CONFLICT, "Insufficient funds", exception);
            default -> accountsUnavailable(exception);
        };
    }

    private CashOperationException accountsUnavailable(Throwable cause) {
        return new CashOperationException(HttpStatus.SERVICE_UNAVAILABLE, "Accounts service is unavailable", cause);
    }
}
