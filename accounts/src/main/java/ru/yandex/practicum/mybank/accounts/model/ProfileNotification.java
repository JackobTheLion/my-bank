package ru.yandex.practicum.mybank.accounts.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_notification")
@Getter
@Setter
@NoArgsConstructor
public class ProfileNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    public ProfileNotification(String login, Instant createdAt) {
        this.login = login;
        this.createdAt = createdAt;
    }

    public void markSent(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
