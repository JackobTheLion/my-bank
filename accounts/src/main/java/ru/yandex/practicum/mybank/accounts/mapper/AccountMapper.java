package ru.yandex.practicum.mybank.accounts.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.mybank.accounts.model.Account;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountRecipientResponse;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    AccountResponse toResponse(Account account);

    AccountRecipientResponse toRecipient(Account account);

    MoneyOperationResponse toMoneyOperationResponse(Account account);

    Account updateAccount(@MappingTarget Account account, AccountProfileRequest request);
}
