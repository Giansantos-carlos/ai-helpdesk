package com.giansantos.helpdesk.assistant.internal.service;

import com.giansantos.helpdesk.knowledgebase.api.ArticleSummary;
import com.giansantos.helpdesk.ticket.api.TicketMessageView;

/** Monta o prompt enviado a IA para sugerir uma resposta de atendente a partir do ContextBundle. */
public final class PromptBuilder {

    private PromptBuilder() {
    }

    public static String build(ContextBundle context) {
        String history = context.messages().isEmpty()
                ? "(sem mensagens ainda)"
                : context.messages().stream().map(PromptBuilder::renderMessage).reduce("", (a, b) -> a + b + "\n");

        String kb = context.articles().isEmpty()
                ? "(nenhum artigo de KB relevante encontrado)"
                : context.articles().stream().map(PromptBuilder::renderArticle).reduce("", (a, b) -> a + b + "\n");

        String requesterName = context.requester() != null ? context.requester().displayName() : "desconhecido";

        return """
                Voce e um assistente que ajuda atendentes de suporte a redigir respostas.
                Assunto do ticket: %s
                Solicitante: %s

                Historico da conversa (mais recentes primeiro):
                %s

                Artigos da base de conhecimento relevantes:
                %s

                Com base apenas nas informacoes acima, escreva uma sugestao de resposta objetiva e
                cordial em portugues para o atendente enviar ao solicitante. Responda APENAS com o
                texto da resposta sugerida, sem explicacoes adicionais.
                """.formatted(context.subject(), requesterName, history, kb);
    }

    private static String renderMessage(TicketMessageView message) {
        return "- autor %s: %s".formatted(message.authorId(), message.body());
    }

    private static String renderArticle(ArticleSummary article) {
        return "### %s\n%s".formatted(article.title(), article.body());
    }
}
