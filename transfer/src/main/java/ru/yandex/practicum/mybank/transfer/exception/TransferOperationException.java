package ru.yandex.practicum.mybank.transfer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransferOperationException extends RuntimeException {

    private final HttpStatus status;

    public TransferOperationException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
