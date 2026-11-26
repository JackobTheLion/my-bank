package ru.yandex.practicum.mybank.accounts.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mybank.accounts.exception.AccountNotFoundException;
import ru.yandex.practicum.mybank.accounts.exception.InvalidAccountOperationException;
import ru.yandex.practicum.mybank.accounts.mapper.AccountMapper;
import ru.yandex.practicum.mybank.accounts.model.Account;
import ru.yandex.practicum.mybank.accounts.repository.AccountRepository;
import ru.yandex.practicum.mybank.accounts.service.MoneyOperationService;
import ru.yandex.practicum.mybank.commons.dto.AccountsCashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.CashAction;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoneyOperationServiceImpl implements MoneyOperationService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public MoneyOperationResponse applyCashOperation(AccountsCashOperationRequest request) {
        Account account = accountRepository.findLockedByLogin(request.login())
                .orElseThrow(AccountNotFoundException::new);
        if (request.action() == CashAction.DEPOSIT) {
            account.deposit(request.amount());
        } else {
            account.withdraw(request.amount());
        }
        return accountMapper.toMoneyOperationResponse(account);
    }

    @Override
    @Transactional
    public MoneyOperationResponse transfer(AccountsTransferRequest request) {
        //Для упрощения не учитываем идемпотетность операций
        String senderLogin = request.senderLogin();
        String recipientLogin = request.recipientLogin();
        if (senderLogin.equals(recipientLogin)) {
            throw new InvalidAccountOperationException("Sender and recipient must differ");
        }

        List<Account> accounts = accountRepository.findLockedByLoginInOrderById(List.of(senderLogin, recipientLogin));
        if (accounts.size() != 2) {
            throw new AccountNotFoundException();
        }
        boolean senderFirst = accounts.getFirst().getLogin().equals(senderLogin);
        Account sender = senderFirst ? accounts.getFirst() : accounts.getLast();
        Account recipient = senderFirst ? accounts.getLast() : accounts.getFirst();

        sender.withdraw(request.amount());
        recipient.deposit(request.amount());
        return accountMapper.toMoneyOperationResponse(sender);
    }
}
