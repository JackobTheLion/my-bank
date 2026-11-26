package ru.yandex.practicum.mybank.accounts.exception;

public class AccountConflictException extends RuntimeException {

    public AccountConflictException(String message) {
        super(message);
    }
}
