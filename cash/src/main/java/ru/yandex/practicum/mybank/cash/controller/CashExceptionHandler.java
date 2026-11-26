package ru.yandex.practicum.mybank.cash.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.mybank.cash.exception.CashOperationException;

@RestControllerAdvice
public class CashExceptionHandler {

    @ExceptionHandler(CashOperationException.class)
    public ProblemDetail handleCashOperation(CashOperationException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
    }
}
