package ru.yandex.practicum.mybank.notifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(properties = "spring.config.location=classpath:/application-test.yaml")
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {

    private static final String NOTIFICATION = """
            {
                "login": "ivanov",
                "type": "PROFILE_UPDATED"
            }
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void logsAnAuthorizedNotification(CapturedOutput output) throws Exception {
        stubToken("notifications:write");

        mvc.perform(post("/internal/notifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOTIFICATION))
                .andExpect(status().isNoContent());

        assertThat(output).contains("Notification received: login=ivanov, type=PROFILE_UPDATED");
    }

    @Test
    void rejectsRequestsWithoutTheNotificationsScope() throws Exception {
        stubToken("accounts:read");

        mvc.perform(post("/internal/notifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOTIFICATION))
                .andExpect(status().isForbidden());
    }

    private void stubToken(String scope) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject("service-account-accounts")
                .claim("scope", scope)
                .build();
        when(jwtDecoder.decode("test-token")).thenReturn(jwt);
    }
}
