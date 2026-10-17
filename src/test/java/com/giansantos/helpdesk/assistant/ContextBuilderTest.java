package com.giansantos.helpdesk.assistant;

import com.giansantos.helpdesk.assistant.config.AssistantContextProperties;
import com.giansantos.helpdesk.assistant.internal.service.ContextBuilder;
import com.giansantos.helpdesk.assistant.internal.service.ContextBundle;
import com.giansantos.helpdesk.knowledgebase.api.ArticleSummary;
import com.giansantos.helpdesk.knowledgebase.api.KnowledgeBaseQueryApi;
import com.giansantos.helpdesk.ticket.api.TicketMessageView;
import com.giansantos.helpdesk.ticket.api.TicketQueryApi;
import com.giansantos.helpdesk.ticket.api.TicketStatus;
import com.giansantos.helpdesk.ticket.api.TicketSummary;
import com.giansantos.helpdesk.user.api.UserQueryApi;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pilar Context Engineering: prova que o ContextBuilder rankeia e trunca historico + artigos
 * de KB em tempo real contra um orcamento de caracteres, descartando primeiro os itens de
 * menor score, em vez de simplesmente cortar a lista por ordem de chegada.
 */
class ContextBuilderTest {

    private final TicketQueryApi ticketQueryApi = mock(TicketQueryApi.class);
    private final KnowledgeBaseQueryApi knowledgeBaseQueryApi = mock(KnowledgeBaseQueryApi.class);
    private final UserQueryApi userQueryApi = mock(UserQueryApi.class);

    @Test
    void dropsLowestScoredArticlesWhenBudgetIsTight() {
        UUID ticketId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        when(ticketQueryApi.findTicket(ticketId)).thenReturn(Optional.of(
                new TicketSummary(ticketId, "Impressora nao liga", TicketStatus.OPEN, requesterId, Instant.now())));
        when(ticketQueryApi.findRecentMessages(ticketId, 10)).thenReturn(List.of());
        when(userQueryApi.findSummary(requesterId)).thenReturn(Optional.empty());

        ArticleSummary highOverlap = new ArticleSummary(UUID.randomUUID(), "Impressora nao liga: solucao",
                "Verifique o cabo de energia da impressora e reinicie.", List.of("impressora"));
        ArticleSummary lowOverlap = new ArticleSummary(UUID.randomUUID(), "Configuracao de rede sem fio",
                "Passos genericos sem relacao direta com o problema relatado pelo usuario.", List.of("rede"));
        when(knowledgeBaseQueryApi.searchByKeywords("Impressora nao liga", 2))
                .thenReturn(List.of(highOverlap, lowOverlap));

        // Orcamento so cabe um dos dois artigos: o de maior overlap de palavras-chave deve ser mantido.
        int budget = ("### " + highOverlap.title() + "\n" + highOverlap.body()).length();
        AssistantContextProperties properties = new AssistantContextProperties(10, 2, budget);
        ContextBuilder contextBuilder = new ContextBuilder(ticketQueryApi, knowledgeBaseQueryApi, userQueryApi, properties);

        ContextBundle bundle = contextBuilder.build(ticketId);

        assertThat(bundle.articles()).containsExactly(highOverlap);
        assertThat(bundle.charsUsed()).isLessThanOrEqualTo(budget);
    }

    @Test
    void prefersMostRecentMessagesOverArticlesWhenBudgetIsVeryTight() {
        UUID ticketId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        when(ticketQueryApi.findTicket(ticketId)).thenReturn(Optional.of(
                new TicketSummary(ticketId, "Erro no sistema", TicketStatus.OPEN, requesterId, Instant.now())));

        TicketMessageView recent = new TicketMessageView(UUID.randomUUID(), requesterId, "Mensagem mais recente", Instant.now());
        when(ticketQueryApi.findRecentMessages(ticketId, 10)).thenReturn(List.of(recent));
        when(userQueryApi.findSummary(requesterId)).thenReturn(Optional.empty());
        when(knowledgeBaseQueryApi.searchByKeywords("Erro no sistema", 2)).thenReturn(List.of(
                new ArticleSummary(UUID.randomUUID(), "Artigo irrelevante para o orcamento",
                        "Corpo bem mais longo do que a mensagem do ticket, nao deve caber no orcamento.", List.of())));

        int tightBudget = ("- autor " + requesterId + ": " + recent.body()).length();
        AssistantContextProperties properties = new AssistantContextProperties(10, 2, tightBudget);
        ContextBuilder contextBuilder = new ContextBuilder(ticketQueryApi, knowledgeBaseQueryApi, userQueryApi, properties);

        ContextBundle bundle = contextBuilder.build(ticketId);

        assertThat(bundle.messages()).containsExactly(recent);
        assertThat(bundle.articles()).isEmpty();
    }
}
