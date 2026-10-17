package com.giansantos.helpdesk.assistant.internal.service;

import com.giansantos.helpdesk.assistant.config.AssistantContextProperties;
import com.giansantos.helpdesk.knowledgebase.api.ArticleSummary;
import com.giansantos.helpdesk.knowledgebase.api.KnowledgeBaseQueryApi;
import com.giansantos.helpdesk.shared.exception.ResourceNotFoundException;
import com.giansantos.helpdesk.ticket.api.TicketMessageView;
import com.giansantos.helpdesk.ticket.api.TicketQueryApi;
import com.giansantos.helpdesk.ticket.api.TicketSummary;
import com.giansantos.helpdesk.user.api.UserQueryApi;
import com.giansantos.helpdesk.user.api.UserSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Peca central do pilar de Context Engineering: busca historico do ticket, artigos de KB e
 * perfil do requester, rankeia cada item por relevancia e monta o maior contexto possivel que
 * ainda cabe no orcamento de caracteres configurado ({@code app.assistant.context.max-chars-budget}),
 * descartando primeiro os itens de menor score. Isso e feito a cada chamada (tempo real), nao
 * pre-computado, porque o conjunto de mensagens/artigos relevantes muda a cada ticket.
 */
@Component
public class ContextBuilder {

    /** Mensagens pesam mais que artigos de KB: historico da conversa e mais relevante que documentacao de apoio. */
    private static final int MESSAGE_SCORE_MULTIPLIER = 100;
    private static final int MIN_KEYWORD_LENGTH = 4;

    private final TicketQueryApi ticketQueryApi;
    private final KnowledgeBaseQueryApi knowledgeBaseQueryApi;
    private final UserQueryApi userQueryApi;
    private final AssistantContextProperties properties;

    public ContextBuilder(TicketQueryApi ticketQueryApi, KnowledgeBaseQueryApi knowledgeBaseQueryApi,
                           UserQueryApi userQueryApi, AssistantContextProperties properties) {
        this.ticketQueryApi = ticketQueryApi;
        this.knowledgeBaseQueryApi = knowledgeBaseQueryApi;
        this.userQueryApi = userQueryApi;
        this.properties = properties;
    }

    public ContextBundle build(UUID ticketId) {
        TicketSummary ticket = ticketQueryApi.findTicket(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket nao encontrado: " + ticketId));

        List<TicketMessageView> recentMessages = ticketQueryApi.findRecentMessages(ticketId, properties.maxMessages());
        Set<String> queryKeywords = extractKeywords(ticket.subject());
        List<ArticleSummary> candidateArticles = knowledgeBaseQueryApi.searchByKeywords(ticket.subject(), properties.maxKbArticles());
        UserSummary requester = userQueryApi.findSummary(ticket.requesterId()).orElse(null);

        List<ScoredItem> pool = new ArrayList<>();
        for (int i = 0; i < recentMessages.size(); i++) {
            int recencyRank = recentMessages.size() - i;
            pool.add(new ScoredItem(ScoredItem.Type.MESSAGE, i, recencyRank * MESSAGE_SCORE_MULTIPLIER,
                    renderMessage(recentMessages.get(i))));
        }
        for (int i = 0; i < candidateArticles.size(); i++) {
            ArticleSummary article = candidateArticles.get(i);
            pool.add(new ScoredItem(ScoredItem.Type.ARTICLE, i, keywordOverlapScore(article, queryKeywords),
                    renderArticle(article)));
        }
        pool.sort(Comparator.comparingInt(ScoredItem::score).reversed());

        int remaining = properties.maxCharsBudget();
        List<Integer> includedMessageIndexes = new ArrayList<>();
        List<Integer> includedArticleIndexes = new ArrayList<>();
        for (ScoredItem item : pool) {
            if (item.text().length() > remaining) {
                continue;
            }
            remaining -= item.text().length();
            if (item.type() == ScoredItem.Type.MESSAGE) {
                includedMessageIndexes.add(item.sourceIndex());
            } else {
                includedArticleIndexes.add(item.sourceIndex());
            }
        }

        List<TicketMessageView> includedMessages = includedMessageIndexes.stream()
                .sorted()
                .map(recentMessages::get)
                .toList();
        List<ArticleSummary> includedArticles = includedArticleIndexes.stream()
                .sorted()
                .map(candidateArticles::get)
                .toList();

        int charsUsed = properties.maxCharsBudget() - remaining;
        return new ContextBundle(ticketId, ticket.subject(), includedMessages, includedArticles, requester,
                properties.maxCharsBudget(), charsUsed);
    }

    private Set<String> extractKeywords(String text) {
        Set<String> words = Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> w.length() >= MIN_KEYWORD_LENGTH)
                .collect(java.util.stream.Collectors.toSet());
        return words.isEmpty() ? Set.of(text.toLowerCase()) : words;
    }

    private int keywordOverlapScore(ArticleSummary article, Set<String> queryKeywords) {
        String haystack = (article.title() + " " + article.body() + " " + String.join(" ", article.tags())).toLowerCase();
        return (int) queryKeywords.stream().filter(haystack::contains).count();
    }

    private String renderMessage(TicketMessageView message) {
        return "- autor %s: %s".formatted(message.authorId(), message.body());
    }

    private String renderArticle(ArticleSummary article) {
        return "### %s\n%s".formatted(article.title(), article.body());
    }

    private record ScoredItem(Type type, int sourceIndex, int score, String text) {
        enum Type { MESSAGE, ARTICLE }
    }
}
