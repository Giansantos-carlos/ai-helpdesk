package com.giansantos.helpdesk.knowledgebase.api;

import com.giansantos.helpdesk.knowledgebase.internal.service.KbArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb/articles")
public class KbArticleController {

    private final KbArticleService articleService;

    public KbArticleController(KbArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody CreateArticleRequest request) {
        var article = articleService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ArticleResponse.from(article));
    }

    @GetMapping
    public List<ArticleResponse> listOrSearch(@RequestParam(name = "q", required = false) String query) {
        return articleService.search(query).stream().map(ArticleResponse::from).toList();
    }
}
