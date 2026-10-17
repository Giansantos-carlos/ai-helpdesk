package com.giansantos.helpdesk.knowledgebase.api;

import com.giansantos.helpdesk.knowledgebase.internal.domain.KbArticle;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleResponse(UUID id, String title, String body, List<String> tags, Instant createdAt) {

    public ArticleResponse {
        tags = List.copyOf(tags);
    }

    public static ArticleResponse from(KbArticle article) {
        return new ArticleResponse(article.getId(), article.getTitle(), article.getBody(), article.getTags(), article.getCreatedAt());
    }
}
