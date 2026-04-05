package edu.model.db.repository;

import edu.model.db.entity.Article;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ArticlesRepository extends JpaRepository<Article, Long> {
    @NotNull
    Article save(@NotNull Article article);

    Optional<Article> findArticleById(Long id);

    Page<Article> findAllByVisibilityTrue(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.likes = a.likes + 1 WHERE a.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Query(value = """
            SELECT a.* FROM articles a
            LEFT JOIN (
                SELECT article_id, COUNT(*) AS comment_count
                FROM comments GROUP BY article_id
            ) c ON c.article_id = a.article_id
            WHERE a.visibility = TRUE
              AND (:query = '' OR a.search_vector @@ plainto_tsquery('russian', :query))
              AND a.likes >= :minLikes
              AND a.views >= :minViews
              AND COALESCE(c.comment_count, 0) >= :minComments
            ORDER BY
                CASE WHEN :sort = 'likes'       THEN a.likes END DESC,
                CASE WHEN :sort = 'views'       THEN a.views END DESC,
                CASE WHEN :sort = 'comments'    THEN COALESCE(c.comment_count, 0) END DESC,
                CASE WHEN :sort = 'updated'     THEN a.last_update_date END DESC,
                CASE WHEN :sort = 'popularity'
                     THEN (a.likes * 3 + a.views + COALESCE(c.comment_count, 0) * 2) END DESC,
                CASE WHEN :sort = 'relevance' AND :query <> ''
                     THEN ts_rank(a.search_vector, plainto_tsquery('russian', :query)) END DESC,
                a.last_update_date DESC
            LIMIT :lim
            """, nativeQuery = true)
    List<Article> searchArticles(
            @Param("query") String query,
            @Param("minLikes") int minLikes,
            @Param("minViews") int minViews,
            @Param("minComments") int minComments,
            @Param("sort") String sort,
            @Param("lim") int limit
    );
}
