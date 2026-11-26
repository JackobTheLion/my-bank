package ru.yandex.practicum.mybank.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mybank.accounts.service.MoneyOperationService;
import ru.yandex.practicum.mybank.commons.dto.AccountsTransferRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/application-test.yaml",
        "accounts.database.schema=accounts_test"
})
@AutoConfigureMockMvc
@Sql(statements = {
        "DELETE FROM accounts_test.profile_notification",
        "DELETE FROM accounts_test.account",
        """
                INSERT INTO accounts_test.account (login, name, birth_date, balance)
                VALUES ('ivanov', 'Иванов Иван', DATE '2001-01-01', 100),
                       ('petrov', 'Петров Пётр', DATE '1998-05-12', 250);
                """
})
class CurrentAccountIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MoneyOperationService moneyOperationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void readsAccountByTokenLoginEvenWhenRequestContainsAnotherLogin() throws Exception {
        stubToken("ivanov", "accounts:read");

        mvc.perform(get("/accounts/me").param("login", "petrov")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("ivanov"))
                .andExpect(jsonPath("$.name").value("Иванов Иван"))
                .andExpect(jsonPath("$.birthDate").value("2001-01-01"))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/accounts/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresAccountsReadScope() throws Exception {
        stubToken("ivanov", "cash:write");

        mvc.perform(get("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requiresUserLoginInToken() throws Exception {
        stubToken(null, "accounts:read");

        mvc.perform(get("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForUserWithoutAnAccount() throws Exception {
        stubToken("sidorov", "accounts:read");

        mvc.perform(get("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Account not found"));
    }

    @Test
    void exposesHealthForConsulWithoutAuthentication() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void storesLiquibaseTablesInConfiguredSchemaAndAppliesDemoSeed() {
        Integer appliedChanges = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts_test.databasechangelog WHERE id = '001-create-account-table'",
                Integer.class
        );
        Integer locks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts_test.databasechangeloglock WHERE id = 1",
                Integer.class
        );
        Integer appliedSeeds = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts_test.databasechangelog WHERE id = '002-seed-demo-accounts'",
                Integer.class
        );

        assertThat(appliedChanges).isEqualTo(1);
        assertThat(locks).isEqualTo(1);
        assertThat(appliedSeeds).isEqualTo(1);
    }

    @Test
    void databaseRejectsNegativeBalances() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE accounts_test.account SET balance = -1 WHERE login = 'ivanov'"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void listsOtherRecipientsWithoutExposingTheirBalance() throws Exception {
        stubToken("ivanov", "accounts:read");

        mvc.perform(get("/accounts/recipients").header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].login").value("petrov"))
                .andExpect(jsonPath("$[0].name").value("Петров Пётр"))
                .andExpect(jsonPath("$[0].balance").doesNotExist())
                .andExpect(jsonPath("$[0].birthDate").doesNotExist());
    }

    @Test
    void updatesOwnProfileAndEnqueuesNotificationForEachRequest() throws Exception {
        stubToken("ivanov", "accounts:write");
        String profile = """
                {"name":"Иванов Иван Иванович","birthDate":"2000-01-01"}
                """;

        mvc.perform(put("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON).content(profile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иванов Иван Иванович"))
                .andExpect(jsonPath("$.balance").value(100));

        mvc.perform(put("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON).content(profile))
                .andExpect(status().isOk());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM accounts_test.profile_notification WHERE login = 'ivanov' AND sent_at IS NULL",
                Integer.class
        )).isEqualTo(2);
    }

    @Test
    void rejectsProfileForUserWhoJustTurnedEighteen() throws Exception {
        stubToken("ivanov", "accounts:write");
        LocalDate birthDate = LocalDate.now().minusYears(18);

        mvc.perform(put("/accounts/me").header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Иванов Иван","birthDate":"%s"}
                                """.formatted(birthDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Account holder must be older than 18"));
    }

    @Test
    void takesAndDepositsCashWithServiceScope() throws Exception {
        stubToken(null, "accounts:cash");

        mvc.perform(post("/internal/accounts/cash-operations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"ivanov","action":"DEPOSIT","amount":20}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(120));

        mvc.perform(post("/internal/accounts/cash-operations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"ivanov","action":"WITHDRAW","amount":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(90));
        assertThat(balance("ivanov")).isEqualTo(90L);
    }

    @Test
    void rejectsTransferFromCashClient() throws Exception {
        stubToken(null, "accounts:cash");

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(20)))
                .andExpect(status().isForbidden());
    }

    @Test
    void appliesEachTransferRequest() throws Exception {
        stubToken(null, "accounts:transfer");

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70));

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(30)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(40));

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(41)))
                .andExpect(status().isConflict());
        assertThat(balance("ivanov")).isEqualTo(40L);
        assertThat(balance("petrov")).isEqualTo(310L);
    }

    @Test
    void rollsBackTransferWhenRecipientBalanceWouldOverflow() throws Exception {
        jdbcTemplate.update("UPDATE accounts_test.account SET balance = ? WHERE login = ?", Long.MAX_VALUE, "petrov");
        stubToken(null, "accounts:transfer");

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson(1)))
                .andExpect(status().isBadRequest());

        assertThat(balance("ivanov")).isEqualTo(100L);
        assertThat(balance("petrov")).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void rejectsTransferToMissingRecipient() throws Exception {
        stubToken(null, "accounts:transfer");

        mvc.perform(post("/internal/accounts/transfers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senderLogin":"ivanov","recipientLogin":"sidorov","amount":20}
                                """))
                .andExpect(status().isNotFound());

        assertThat(balance("ivanov")).isEqualTo(100L);
    }

    @Test
    void appliesConcurrentTransfersWithoutLosingBalanceUpdates() throws Exception {
        AccountsTransferRequest request = new AccountsTransferRequest("ivanov", "petrov", 30);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<MoneyOperationResponse> first = executor.submit(() -> moneyOperationService.transfer(request));
            Future<MoneyOperationResponse> second = executor.submit(() -> moneyOperationService.transfer(request));

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }

        assertThat(balance("ivanov")).isEqualTo(40L);
        assertThat(balance("petrov")).isEqualTo(310L);
    }

    private Long balance(String login) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM accounts_test.account WHERE login = ?", Long.class, login
        );
    }

    private String transferJson(long amount) {
        return """
                {"senderLogin":"ivanov","recipientLogin":"petrov","amount":%d}
                """.formatted(amount);
    }

    private void stubToken(String login, String scope) {
        Jwt.Builder token = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("test-user")
                .claim("scope", scope);
        if (login != null) {
            token.claim("preferred_username", login);
        }
        when(jwtDecoder.decode("test-token")).thenReturn(token.build());
    }
}
