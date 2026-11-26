package ru.yandex.practicum.exception;

public class MoneyOperationRejectedException extends RuntimeException {

    public MoneyOperationRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
