package ru.yandex.practicum.mybank.commons.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtUserLogin {

    private JwtUserLogin() {
    }

    public static String requireLogin(Jwt token) {
        Object loginClaim = token.getClaims().get("preferred_username");
        if (!(loginClaim instanceof String login) || login.isBlank()) {
            throw new AccessDeniedException("JWT must contain a user login");
        }
        return login;
    }
}
