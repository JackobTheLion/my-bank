package ru.yandex.practicum.mybank.accounts.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException() {
        super("Account not found");
    }
}
