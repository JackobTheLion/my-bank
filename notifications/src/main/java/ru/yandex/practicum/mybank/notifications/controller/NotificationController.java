package ru.yandex.practicum.mybank.notifications.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;

public interface NotificationController {

    @PostMapping("/internal/notifications")
    @PreAuthorize("hasAuthority('SCOPE_notifications:write')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void receive(@Valid @RequestBody NotificationRequest request);
}
