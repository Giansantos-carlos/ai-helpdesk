package com.giansantos.helpdesk.knowledgebase.internal.service;

import com.giansantos.helpdesk.knowledgebase.api.ArticleSummary;
import com.giansantos.helpdesk.knowledgebase.api.CreateArticleRequest;
import com.giansantos.helpdesk.knowledgebase.api.KnowledgeBaseQueryApi;
import com.giansantos.helpdesk.knowledgebase.internal.domain.KbArticle;
import com.giansantos.helpdesk.knowledgebase.internal.repo.KbArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KbArticleService implements KnowledgeBaseQueryApi {

    private static final int DEFAULT_LIST_LIMIT = 50;

    private final KbArticleRepository articleRepository;

    public KbArticleService(KbArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public KbArticle createArticle(CreateArticleRequest request) {
        KbArticle article = KbArticle.create(request.title(), request.body(), request.tags());
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public List<KbArticle> search(String query) {
        if (query == null || query.isBlank()) {
            return articleRepository.findAllByOrderByCreatedAtDesc();
        }
        return articleRepository.searchByKeywords(query, DEFAULT_LIST_LIMIT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleSummary> searchByKeywords(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return articleRepository.searchByKeywords(query, limit).stream()
                .map(a -> new ArticleSummary(a.getId(), a.getTitle(), a.getBody(), a.getTags()))
                .toList();
    }
}
