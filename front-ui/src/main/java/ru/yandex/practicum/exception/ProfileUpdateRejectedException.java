package ru.yandex.practicum.exception;

public class ProfileUpdateRejectedException extends RuntimeException {

    public ProfileUpdateRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
