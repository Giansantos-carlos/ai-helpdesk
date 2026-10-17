package com.giansantos.helpdesk.knowledgebase.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "kb_articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KbArticle {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar[]")
    private List<String> tags;

    @Column(nullable = false)
    private Instant createdAt;

    public static KbArticle create(String title, String body, List<String> tags) {
        KbArticle article = new KbArticle();
        article.id = UUID.randomUUID();
        article.title = title;
        article.body = body;
        article.tags = tags == null ? List.of() : tags;
        article.createdAt = Instant.now();
        return article;
    }
}
