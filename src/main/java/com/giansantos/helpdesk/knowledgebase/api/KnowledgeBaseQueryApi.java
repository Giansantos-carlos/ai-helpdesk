package com.giansantos.helpdesk.knowledgebase.api;

import java.util.List;

/** Superficie publica do modulo knowledgebase para busca, usada por outros modulos (ex: assistant). */
public interface KnowledgeBaseQueryApi {
    List<ArticleSummary> searchByKeywords(String query, int limit);
}
