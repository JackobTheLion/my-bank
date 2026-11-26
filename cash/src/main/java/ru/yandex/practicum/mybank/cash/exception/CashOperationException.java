package ru.yandex.practicum.mybank.cash.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CashOperationException extends RuntimeException {

    private final HttpStatus status;

    public CashOperationException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
