package ru.yandex.practicum.mybank.accounts.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import ru.yandex.practicum.mybank.accounts.model.ProfileNotification;

import java.util.Optional;
import java.util.UUID;

public interface ProfileNotificationRepository extends JpaRepository<ProfileNotification, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProfileNotification> findFirstBySentAtIsNullOrderByCreatedAtAscIdAsc();
}
