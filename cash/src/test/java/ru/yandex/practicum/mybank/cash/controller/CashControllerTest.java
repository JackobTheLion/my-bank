package ru.yandex.practicum.mybank.cash.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.mybank.cash.config.CashSecurityConfiguration;
import ru.yandex.practicum.mybank.cash.controller.impl.CashControllerImpl;
import ru.yandex.practicum.mybank.cash.exception.CashOperationException;
import ru.yandex.practicum.mybank.cash.service.CashOperationService;
import ru.yandex.practicum.mybank.commons.dto.CashAction;
import ru.yandex.practicum.mybank.commons.dto.CashOperationRequest;
import ru.yandex.practicum.mybank.commons.dto.MoneyOperationResponse;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CashControllerImpl.class, properties = "spring.config.location=classpath:/application-test.yaml")
@Import(CashSecurityConfiguration.class)
class CashControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CashOperationService cashOperationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void usesLoginFromTokenEvenWhenRequestContainsAnotherLogin() throws Exception {
        stubToken("cash:write", "ivanov");
        CashOperationRequest request = new CashOperationRequest(CashAction.DEPOSIT, 100);
        when(cashOperationService.applyCashOperation("ivanov", request)).thenReturn(new MoneyOperationResponse(1_100L));

        submit("""
                {"action":"DEPOSIT","amount":100,"login":"petrov"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1_100));
    }

    @Test
    void rejectsTokenWithoutCashScope() throws Exception {
        stubToken("accounts:read", "ivanov");

        submit("""
                {"action":"DEPOSIT","amount":100}
                """).andExpect(status().isForbidden());
        verifyNoInteractions(cashOperationService);
    }

    @Test
    void rejectsTokenWithoutUserLogin() throws Exception {
        stubToken("cash:write", " ");

        submit("""
                {"action":"DEPOSIT","amount":100}
                """).andExpect(status().isForbidden());
        verifyNoInteractions(cashOperationService);
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        stubToken("cash:write", "ivanov");

        submit("""
                {"action":"WITHDRAW","amount":0}
                """).andExpect(status().isBadRequest());
        verifyNoInteractions(cashOperationService);
    }

    @Test
    void exposesAnInsufficientBalanceAsAConflict() throws Exception {
        stubToken("cash:write", "ivanov");
        CashOperationRequest request = new CashOperationRequest(CashAction.WITHDRAW, 100);
        when(cashOperationService.applyCashOperation("ivanov", request))
                .thenThrow(new CashOperationException(HttpStatus.CONFLICT, "Insufficient funds", null));

        submit("""
                {"action":"WITHDRAW","amount":100}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Insufficient funds"));
    }

    private ResultActions submit(String body) throws Exception {
        return mvc.perform(post("/cash/operations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void stubToken(String scope, String login) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("ivanov-subject")
                .claim("scope", scope)
                .claim("preferred_username", login)
                .build();
        when(jwtDecoder.decode("test-token")).thenReturn(jwt);
    }
}
