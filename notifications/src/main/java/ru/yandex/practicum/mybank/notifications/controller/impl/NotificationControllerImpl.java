package ru.yandex.practicum.mybank.notifications.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;
import ru.yandex.practicum.mybank.notifications.controller.NotificationController;
import ru.yandex.practicum.mybank.notifications.service.NotificationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {

    private final List<NotificationService> notificationServices;

    @Override
    public void receive(NotificationRequest request) {
        notificationServices.forEach(notificationService -> notificationService.notify(request));
    }
}
