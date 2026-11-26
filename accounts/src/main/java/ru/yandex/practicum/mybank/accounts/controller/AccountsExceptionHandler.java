package ru.yandex.practicum.mybank.accounts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.mybank.accounts.exception.AccountConflictException;
import ru.yandex.practicum.mybank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.mybank.accounts.exception.InvalidAccountAgeException;
import ru.yandex.practicum.mybank.accounts.exception.InvalidAccountOperationException;

@RestControllerAdvice
public class AccountsExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({InvalidAccountAgeException.class, InvalidAccountOperationException.class})
    public ProblemDetail handleInvalidAccountAge(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AccountConflictException.class)
    public ProblemDetail handleAccountConflict(AccountConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
