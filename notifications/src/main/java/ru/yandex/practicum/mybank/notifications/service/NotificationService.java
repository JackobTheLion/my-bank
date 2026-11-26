package ru.yandex.practicum.mybank.notifications.service;

import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;

public interface NotificationService {

    void notify(NotificationRequest request);
}
