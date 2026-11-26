package ru.yandex.practicum.mybank.accounts.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.yandex.practicum.mybank.accounts.model.Account;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByLogin(String login);

    boolean existsByLogin(String login);

    List<Account> findAllByLoginNotOrderByNameAscLoginAsc(String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findLockedByLogin(String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Account> findLockedByLoginInOrderById(Collection<String> logins);
}
