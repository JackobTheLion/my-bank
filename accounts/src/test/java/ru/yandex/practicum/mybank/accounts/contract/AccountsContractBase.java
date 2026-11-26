package ru.yandex.practicum.mybank.accounts.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/application-test.yaml",
        "accounts.database.schema=accounts_contract"
})
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM accounts_contract.profile_notification",
        "DELETE FROM accounts_contract.account",
        """
                INSERT INTO accounts_contract.account (login, name, birth_date, balance)
                VALUES ('ivanov', 'Иванов Иван', DATE '2001-01-01', 100),
                       ('petrov', 'Петров Пётр', DATE '1998-05-12', 250);
                """
})
public abstract class AccountsContractBase {

    private static final String TOKEN = "accounts-contract-token";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpContractTest() {
        Jwt jwt = Jwt.withTokenValue(TOKEN)
                .header("alg", "RS256")
                .subject("contract-user")
                .claim("scope", "accounts:read accounts:write")
                .claim("preferred_username", "ivanov")
                .build();
        when(jwtDecoder.decode(TOKEN)).thenReturn(jwt);
        stubServiceToken("accounts-cash-contract-token", "cash", "accounts:cash");
        stubServiceToken("accounts-transfer-contract-token", "transfer", "accounts:transfer");
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @AfterEach
    void resetContractTest() {
        RestAssuredMockMvc.reset();
    }

    private void stubServiceToken(String token, String client, String scope) {
        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "RS256")
                .subject("service-account-" + client)
                .claim("scope", scope)
                .build();
        when(jwtDecoder.decode(token)).thenReturn(jwt);
    }
}
