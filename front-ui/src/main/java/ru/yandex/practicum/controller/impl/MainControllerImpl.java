package ru.yandex.practicum.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mybank.commons.dto.AccountProfileRequest;
import ru.yandex.practicum.mybank.commons.dto.AccountResponse;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.TransferRequest;
import ru.yandex.practicum.controller.MainController;
import ru.yandex.practicum.controller.dto.AccountProfileForm;
import ru.yandex.practicum.controller.dto.CashOperationForm;
import ru.yandex.practicum.controller.dto.TransferForm;
import ru.yandex.practicum.exception.MoneyOperationRejectedException;
import ru.yandex.practicum.exception.ProfileUpdateRejectedException;
import ru.yandex.practicum.service.AccountsClient;
import ru.yandex.practicum.service.MoneyOperationsClient;

@Controller
@RequiredArgsConstructor
public class MainControllerImpl implements MainController {

    private final AccountsClient accountsClient;
    private final MoneyOperationsClient moneyOperationsClient;

    @Override
    public String index() {
        return "redirect:/account";
    }

    @Override
    public String getAccount(Model model) {
        return renderAccount(model);
    }

    @Override
    public String updateAccount(
            AccountProfileForm profile,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return submitForm(errors, model, redirectAttributes, "Профиль сохранён", "profile.update.rejected",
                () -> accountsClient.updateProfile(new AccountProfileRequest(profile.name(), profile.birthDate())));
    }

    @Override
    public String applyCashOperation(
            CashOperationForm operation,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return submitForm(errors, model, redirectAttributes, "Операция выполнена", "cash.operation.rejected",
                () -> moneyOperationsClient.applyCashOperation(new CashOperationRequest(operation.action(), operation.amount())));
    }

    @Override
    public String transfer(
            TransferForm transfer,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return submitForm(errors, model, redirectAttributes, "Перевод выполнен", "transfer.rejected",
                () -> moneyOperationsClient.transfer(new TransferRequest(transfer.recipientLogin(), transfer.amount())));
    }

    private String submitForm(
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes,
            String successMessage,
            String rejectionCode,
            Runnable action
    ) {
        if (errors.hasErrors()) {
            return renderAccount(model);
        }
        try {
            action.run();
        } catch (ProfileUpdateRejectedException | MoneyOperationRejectedException exception) {
            errors.reject(rejectionCode, exception.getMessage());
            return renderAccount(model);
        }
        redirectAttributes.addFlashAttribute("info", successMessage);
        return "redirect:/account";
    }

    private String renderAccount(Model model) {
        AccountResponse account = accountsClient.getCurrentAccount();
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new AccountProfileForm(account.name(), account.birthDate()));
        }
        if (!model.containsAttribute("cashForm")) {
            model.addAttribute("cashForm", new CashOperationForm(null, null));
        }
        if (!model.containsAttribute("transferForm")) {
            model.addAttribute("transferForm", new TransferForm(null, null));
        }
        model.addAttribute("sum", account.balance());
        model.addAttribute("accounts", accountsClient.listRecipients());
        return "main";
    }
}
