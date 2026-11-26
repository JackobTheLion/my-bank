package ru.yandex.practicum.mybank.accounts.exception;

public class InvalidAccountAgeException extends RuntimeException {

    public InvalidAccountAgeException() {
        super("Account holder must be older than 18");
    }
}
