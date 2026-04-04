package edu.model.db.repository;

import edu.model.db.entity.Article;
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
}
