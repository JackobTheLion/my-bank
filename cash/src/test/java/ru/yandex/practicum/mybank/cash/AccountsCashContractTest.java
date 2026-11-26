package ru.yandex.practicum.mybank.cash;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerPort;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.mybank.cash.exception.CashOperationException;
import ru.yandex.practicum.mybank.cash.service.CashOperationService;
import ru.yandex.practicum.mybank.commons.dto.CashAction;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest(properties = {
        "spring.config.location=classpath:/application-test.yaml",
        "stubrunner.cloud.loadbalancer.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/unused",
        "spring.security.oauth2.client.registration.cash.provider=keycloak",
        "spring.security.oauth2.client.registration.cash.client-id=cash",
        "spring.security.oauth2.client.registration.cash.client-secret=contract-test-secret",
        "spring.security.oauth2.client.registration.cash.authorization-grant-type=client_credentials",
        "spring.security.oauth2.client.provider.keycloak.token-uri=http://127.0.0.1:${stubrunner.runningstubs.accounts.port}/token"
})
@AutoConfigureStubRunner(ids = "ru.yandex.practicum:accounts", stubsMode = StubRunnerProperties.StubsMode.CLASSPATH)
class AccountsCashContractTest {

    @Autowired
    private CashOperationService cashOperations;

    @StubRunnerPort("accounts")
    private int accountsStubPort;

    @BeforeEach
    void stubClientCredentialsToken() {
        WireMock stub = new WireMock("127.0.0.1", accountsStubPort);
        stub.register(post(urlEqualTo("/token"))
                .withRequestBody(containing("grant_type=client_credentials"))
                .willReturn(okJson("""
                        {"access_token":"cash-test-token","token_type":"Bearer","expires_in":300}
                        """)));
    }

    @Test
    void depositsCashThroughTheAccountsContract() {
        CashOperationRequest request = new CashOperationRequest(CashAction.DEPOSIT, 25);
        MoneyOperationResponse response = cashOperations.applyCashOperation("ivanov", request);
        assertThat(response.balance()).isEqualTo(125L);
    }

    @Test
    void reportsInsufficientFundsFromTheAccountsContract() {
        CashOperationRequest request = new CashOperationRequest(CashAction.WITHDRAW, 101);
        assertThatExceptionOfType(CashOperationException.class)
                .isThrownBy(() -> cashOperations.applyCashOperation("ivanov", request))
                .satisfies(exception -> assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT))
                .withMessage("Insufficient funds");
    }
}
