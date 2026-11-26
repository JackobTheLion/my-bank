package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.controller.dto.AccountProfileForm;
import ru.yandex.practicum.controller.dto.CashOperationForm;
import ru.yandex.practicum.controller.dto.TransferForm;

public interface MainController {

    @GetMapping("/")
    String index();

    @GetMapping("/account")
    String getAccount(Model model);

    @PostMapping("/account")
    String updateAccount(
            @Valid @ModelAttribute("profileForm") AccountProfileForm profile,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    );

    @PostMapping("/cash")
    String applyCashOperation(
            @Valid @ModelAttribute("cashForm") CashOperationForm operation,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    );

    @PostMapping("/transfer")
    String transfer(
            @Valid @ModelAttribute("transferForm") TransferForm transfer,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    );
}
