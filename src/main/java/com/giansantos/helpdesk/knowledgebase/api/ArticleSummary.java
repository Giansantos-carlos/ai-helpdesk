package com.giansantos.helpdesk.knowledgebase.api;

import java.util.List;
import java.util.UUID;

/** Visao minima de um artigo de KB, exposta a outros modulos (ex: assistant monta contexto). */
public record ArticleSummary(UUID id, String title, String body, List<String> tags) {

    public ArticleSummary {
        tags = List.copyOf(tags);
    }
}
