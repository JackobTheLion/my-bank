package ru.yandex.practicum.mybank.notifications.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybank.commons.dto.NotificationRequest;
import ru.yandex.practicum.mybank.notifications.service.NotificationService;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "notifications.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notify(NotificationRequest request) {
        log.info("Notification received: login={}, type={}", request.login(), request.type());
    }
}
