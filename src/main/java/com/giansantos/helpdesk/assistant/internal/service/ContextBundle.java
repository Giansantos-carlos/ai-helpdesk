package com.giansantos.helpdesk.assistant.internal.service;

import com.giansantos.helpdesk.knowledgebase.api.ArticleSummary;
import com.giansantos.helpdesk.ticket.api.TicketMessageView;
import com.giansantos.helpdesk.user.api.UserSummary;

import java.util.List;
import java.util.UUID;

/**
 * Contexto montado e otimizado em tempo real para uma sugestao de resposta: historico do
 * ticket + artigos de KB relevantes, rankeados e truncados pelo {@link ContextBuilder} contra
 * um orcamento de caracteres (heuristica de token budget, ver ContextBuilder).
 */
public record ContextBundle(
        UUID ticketId,
        String subject,
        List<TicketMessageView> messages,
        List<ArticleSummary> articles,
        UserSummary requester,
        int charsBudget,
        int charsUsed) {

    public ContextBundle {
        messages = List.copyOf(messages);
        articles = List.copyOf(articles);
    }
}
