package ru.yandex.practicum.mybank.transfer.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.mybank.transfer.exception.TransferOperationException;

@RestControllerAdvice
public class TransferExceptionHandler {

    @ExceptionHandler(TransferOperationException.class)
    public ProblemDetail handleTransferFailure(TransferOperationException failure) {
        return ProblemDetail.forStatusAndDetail(failure.getStatus(), failure.getMessage());
    }
}
