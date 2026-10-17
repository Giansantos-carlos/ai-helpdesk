package com.giansantos.helpdesk.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Orcamento/limites usados pelo ContextBuilder para montar o contexto em tempo real. */
@ConfigurationProperties(prefix = "app.assistant.context")
public record AssistantContextProperties(int maxMessages, int maxKbArticles, int maxCharsBudget) {
}
