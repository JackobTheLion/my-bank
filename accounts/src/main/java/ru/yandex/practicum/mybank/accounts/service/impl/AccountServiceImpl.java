package ru.yandex.practicum.mybank.accounts.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mybank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.mybank.accounts.exception.InvalidAccountAgeException;
import ru.yandex.practicum.mybank.accounts.mapper.AccountMapper;
import ru.yandex.practicum.mybank.accounts.model.Account;
import ru.yandex.practicum.mybank.accounts.model.ProfileNotification;
import ru.yandex.practicum.mybank.accounts.repository.AccountRepository;
import ru.yandex.practicum.mybank.accounts.repository.ProfileNotificationRepository;
import ru.yandex.practicum.mybank.accounts.service.AccountService;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ProfileNotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getByLogin(String login) {
        return accountRepository.findByLogin(login)
                .map(accountMapper::toResponse)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Override
    @Transactional
    public AccountResponse updateProfile(String login, AccountProfileRequest request) {
        LocalDate ageLimit = LocalDate.now().minusYears(18);
        if (!request.birthDate().isBefore(ageLimit)) {
            throw new InvalidAccountAgeException();
        }

        Account account = accountRepository.findLockedByLogin(login)
                .map(existing -> accountMapper.updateAccount(existing, request))
                .orElseThrow(AccountNotFoundException::new);
        notificationRepository.save(new ProfileNotification(login, Instant.now()));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountRecipientResponse> listRecipients(String login) {
        if (!accountRepository.existsByLogin(login)) {
            throw new AccountNotFoundException();
        }
        return accountRepository.findAllByLoginNotOrderByNameAscLoginAsc(login).stream()
                .map(accountMapper::toRecipient)
                .toList();
    }
}
