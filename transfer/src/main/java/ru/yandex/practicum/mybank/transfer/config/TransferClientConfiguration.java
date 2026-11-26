package ru.yandex.practicum.mybank.transfer.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal;

@Configuration(proxyBeanMethods = false)
public class TransferClientConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder transferClientBuilder(
            RestClientBuilderConfigurer configurer,
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService authorizedClients
    ) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, authorizedClients);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build());
        OAuth2ClientHttpRequestInterceptor authorization = new OAuth2ClientHttpRequestInterceptor(manager);
        authorization.setPrincipalResolver(new RequestAttributePrincipalResolver());
        authorization.setAuthorizationFailureHandler(
                OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClients)
        );

        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return configurer.configure(RestClient.builder())
                .requestFactory(requestFactory)
                .requestInterceptor(authorization)
                .defaultRequest(request -> request
                        .attributes(clientRegistrationId("transfer"))
                        .attributes(principal("transfer")));
    }

    @Bean
    public RestClient transferClient(@LoadBalanced RestClient.Builder builder) {
        return builder.build();
    }
}
