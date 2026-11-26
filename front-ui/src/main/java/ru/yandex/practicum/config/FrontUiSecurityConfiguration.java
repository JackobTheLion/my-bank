package ru.yandex.practicum.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class FrontUiSecurityConfiguration {

    @Bean
    public SecurityFilterChain frontUiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(login -> login.loginPage("/oauth2/authorization/keycloak"))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
