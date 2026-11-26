package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class GatewayClientConfiguration {

    @Bean
    public RestClient gatewayRestClient(
            RestClient.Builder builder,
            @Value("${gateway.base-url}") String baseUrl,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        OAuth2ClientHttpRequestInterceptor interceptor =
                new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        interceptor.setClientRegistrationIdResolver(request -> "keycloak");
        return builder.baseUrl(baseUrl)
                .requestInterceptor(interceptor)
                .build();
    }
}
