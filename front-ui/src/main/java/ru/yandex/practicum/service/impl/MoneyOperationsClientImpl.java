package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;
import ru.yandex.practicum.exception.MoneyOperationRejectedException;
import ru.yandex.practicum.service.MoneyOperationsClient;

@Service
@RequiredArgsConstructor
public class MoneyOperationsClientImpl implements MoneyOperationsClient {

    private static final String UNCONFIRMED = "Не удалось подтвердить операцию. Попробуйте позднее.";

    private final RestClient gateway;

    @Override
    public void applyCashOperation(CashOperationRequest operation) {
        post("/cash/operations", operation);
    }

    @Override
    public void transfer(TransferRequest transfer) {
        post("/transfer/operations", transfer);
    }

    private void post(String path, Object body) {
        try {
            gateway.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            String message = switch (exception.getStatusCode().value()) {
                case 400 -> "Проверьте сумму и параметры операции";
                case 401, 403 -> "Войдите заново, чтобы выполнить операцию";
                case 404 -> "Счёт не найден";
                case 409 -> "Недостаточно средств";
                default -> UNCONFIRMED;
            };
            throw new MoneyOperationRejectedException(message, exception);
        } catch (RestClientException exception) {
            throw new MoneyOperationRejectedException(UNCONFIRMED, exception);
        }
    }
}
