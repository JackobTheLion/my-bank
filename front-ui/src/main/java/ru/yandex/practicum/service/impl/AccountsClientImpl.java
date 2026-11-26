package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;
import ru.yandex.practicum.exception.ProfileUpdateRejectedException;
import ru.yandex.practicum.service.AccountsClient;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountsClientImpl implements AccountsClient {

    private final RestClient gateway;

    @Override
    public AccountResponse getCurrentAccount() {
        AccountResponse account = gateway.get()
                .uri("/accounts/me")
                .retrieve()
                .body(AccountResponse.class);
        return Objects.requireNonNull(account, "Accounts response must contain a profile");
    }

    @Override
    public void updateProfile(AccountProfileRequest profile) {
        try {
            gateway.put()
                    .uri("/accounts/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(profile)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.BadRequest exception) {
            throw new ProfileUpdateRejectedException("Проверьте имя и дату рождения", exception);
        } catch (HttpClientErrorException.Forbidden exception) {
            throw new ProfileUpdateRejectedException("Войдите заново, чтобы редактировать профиль", exception);
        }
    }

    @Override
    public List<AccountRecipientResponse> listRecipients() {
        List<AccountRecipientResponse> recipients = gateway.get()
                .uri("/accounts/recipients")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AccountRecipientResponse>>() {});
        return Objects.requireNonNull(recipients, "Accounts response must contain recipients");
    }

}
