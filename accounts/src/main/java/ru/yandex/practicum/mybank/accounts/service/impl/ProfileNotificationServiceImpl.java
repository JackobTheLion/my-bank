package ru.yandex.practicum.mybank.accounts.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.yandex.practicum.mybank.accounts.mapper.ProfileNotificationMapper;
import ru.yandex.practicum.mybank.accounts.model.ProfileNotification;
import ru.yandex.practicum.mybank.accounts.repository.ProfileNotificationRepository;
import ru.yandex.practicum.mybank.accounts.service.ProfileNotificationService;

import java.time.Instant;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileNotificationServiceImpl implements ProfileNotificationService {

    private final ProfileNotificationRepository notificationRepository;
    private final ProfileNotificationMapper notificationMapper;
    private final RestClient notificationsClient;
    private final DiscoveryClient discoveryClient;

    @Override
    @Scheduled(fixedDelayString = "${accounts.notifications.poll-delay:PT5S}")
    @Transactional
    public void deliverNext() {
        Optional<ProfileNotification> pending = notificationRepository.findFirstBySentAtIsNullOrderByCreatedAtAscIdAsc();
        if (pending.isEmpty()) {
            return;
        }

        ProfileNotification notification = pending.get();
        try {
            ServiceInstance notifications = discoveryClient.getInstances("notifications").stream()
                    .findFirst()
                    .orElseThrow(() -> new RestClientException("Notifications service is unavailable"));
            notificationsClient.post()
                    .uri(notifications.getUri().resolve("/internal/notifications"))
                    .attributes(clientRegistrationId("notifications"))
                    .attributes(principal("accounts"))
                    .body(notificationMapper.toRequest(notification))
                    .retrieve()
                    .toBodilessEntity();
            notification.markSent(Instant.now());
        } catch (RestClientException | OAuth2AuthorizationException exception) {
            log.warn("Profile notification {} delivery failed: {}", notification.getId(), exception.getMessage());
        }
    }
}
