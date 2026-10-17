package com.giansantos.helpdesk.knowledgebase.internal.repo;

import com.giansantos.helpdesk.knowledgebase.internal.domain.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface KbArticleRepository extends JpaRepository<KbArticle, UUID> {

    List<KbArticle> findAllByOrderByCreatedAtDesc();

    // tsvector e computado em tempo de consulta (nao em uma coluna gerada): o Postgres exige que
    // a expressao de uma coluna GENERATED ... STORED seja IMMUTABLE, e to_tsvector(text, text)
    // com o nome da configuracao como texto e apenas STABLE.
    @Query(value = """
            SELECT * FROM kb_articles
            WHERE to_tsvector('portuguese', title || ' ' || body || ' ' || array_to_string(tags, ' '))
                  @@ plainto_tsquery('portuguese', :query)
            ORDER BY ts_rank(
                to_tsvector('portuguese', title || ' ' || body || ' ' || array_to_string(tags, ' ')),
                plainto_tsquery('portuguese', :query)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KbArticle> searchByKeywords(@Param("query") String query, @Param("limit") int limit);
}
