package ru.yandex.practicum.mybank.accounts;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.mybank.accounts.config.NotificationsClientConfiguration;
import ru.yandex.practicum.mybank.accounts.mapper.ProfileNotificationMapper;
import ru.yandex.practicum.mybank.accounts.model.ProfileNotification;
import ru.yandex.practicum.mybank.accounts.repository.ProfileNotificationRepository;
import ru.yandex.practicum.mybank.accounts.service.impl.ProfileNotificationServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ProfileNotificationServiceTest {

    private static final String REQUEST_BODY = """
            {"login":"ivanov","type":"PROFILE_UPDATED"}
            """;

    @Test
    void retainsUnsentNotificationAfterFailureAndMarksItSentAfterRetry() {
        ProfileNotification notification = new ProfileNotification("ivanov", Instant.now());
        ProfileNotificationRepository repository = mock(ProfileNotificationRepository.class);
        when(repository.findFirstBySentAtIsNullOrderByCreatedAtAscIdAsc()).thenReturn(Optional.of(notification));
        ProfileNotificationMapper mapper = Mappers.getMapper(ProfileNotificationMapper.class);

        RestClient.Builder builder = notificationsClientWithCachedToken().mutate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        when(discovery.getInstances("notifications")).thenReturn(List.of())
                .thenReturn(List.of(
                        new DefaultServiceInstance("notifications-1", "notifications", "notifications-host", 9085, false)
                ));
        ProfileNotificationServiceImpl service = new ProfileNotificationServiceImpl(
                repository, mapper, builder.build(), discovery
        );
        server.expect(requestTo("http://notifications-host:9085/internal/notifications"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer service-token"))
                .andExpect(content().json(REQUEST_BODY, JsonCompareMode.STRICT))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo("http://notifications-host:9085/internal/notifications"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer service-token"))
                .andExpect(content().json(REQUEST_BODY, JsonCompareMode.STRICT))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        service.deliverNext();
        assertThat(notification.getSentAt()).isNull();

        service.deliverNext();
        assertThat(notification.getSentAt()).isNull();

        service.deliverNext();
        assertThat(notification.getSentAt()).isNotNull();
        server.verify();
    }

    private RestClient notificationsClientWithCachedToken() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("notifications")
                .clientId("accounts")
                .clientSecret("test-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://keycloak/token")
                .build();
        InMemoryClientRegistrationRepository registrations = new InMemoryClientRegistrationRepository(registration);
        InMemoryOAuth2AuthorizedClientService authorizedClients = new InMemoryOAuth2AuthorizedClientService(registrations);
        Instant now = Instant.now();
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER, "service-token", now, now.plusSeconds(300),
                Set.of("notifications:write")
        );
        authorizedClients.saveAuthorizedClient(
                new OAuth2AuthorizedClient(registration, "accounts", token),
                UsernamePasswordAuthenticationToken.unauthenticated("accounts", null)
        );
        return new NotificationsClientConfiguration()
                .notificationsClient(RestClient.builder(), registrations, authorizedClients);
    }
}
