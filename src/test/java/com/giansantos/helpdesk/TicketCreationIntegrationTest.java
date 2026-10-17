package com.giansantos.helpdesk;

import com.giansantos.helpdesk.assistant.api.SuggestionResponse;
import com.giansantos.helpdesk.assistant.api.SuggestionStatus;
import com.giansantos.helpdesk.ticket.api.CreateTicketRequest;
import com.giansantos.helpdesk.ticket.api.TicketResponse;
import com.giansantos.helpdesk.user.api.CreateUserRequest;
import com.giansantos.helpdesk.user.api.UserResponse;
import com.giansantos.helpdesk.user.api.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prova o fluxo decacoplado ponta a ponta, apenas via API publica (HTTP): criar um ticket deve
 * disparar de forma assincrona o listener do modulo assistant, que monta contexto e tenta
 * chamar a IA. Sem ANTHROPIC_API_KEY configurada no ambiente de teste, a sugestao deve terminar
 * em FAILED (degradacao graciosa), nunca travar a requisicao original de criacao do ticket.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TicketCreationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("helpdesk")
            .withUsername("helpdesk")
            .withPassword("helpdesk");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void creatingTicket_triggersAsyncAssistantSuggestion() {
        var user = restTemplate.postForEntity(url("/api/users"),
                new CreateUserRequest("requester@helpdesk.test", "Requester", UserRole.AGENT), UserResponse.class);
        assertThat(user.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var ticket = restTemplate.postForEntity(url("/api/tickets"),
                new CreateTicketRequest("Impressora nao liga", user.getBody().id()), TicketResponse.class);
        assertThat(ticket.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ResponseEntity<SuggestionResponse> suggestion = restTemplate.getForEntity(
                    url("/api/tickets/" + ticket.getBody().id() + "/suggestion"), SuggestionResponse.class);
            assertThat(suggestion.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(suggestion.getBody().status()).isEqualTo(SuggestionStatus.FAILED);
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
