package ru.yandex.practicum.mybank.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.mybank.accounts.exception.AccountConflictException;
import ru.yandex.practicum.mybank.accounts.exception.InvalidAccountOperationException;

import java.time.LocalDate;

@Entity
@Table(name = "account")
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private long balance;

    public void deposit(long amount) {
        if (amount > Long.MAX_VALUE - balance) {
            throw new InvalidAccountOperationException("Balance limit exceeded");
        }
        balance += amount;
    }

    public void withdraw(long amount) {
        if (amount > balance) {
            throw new AccountConflictException("Insufficient funds");
        }
        balance -= amount;
    }
}
