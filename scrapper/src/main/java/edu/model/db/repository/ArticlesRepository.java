package edu.model.db.repository;

import edu.model.db.entity.Article;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticlesRepository extends JpaRepository<Article, Long> {
    @NotNull
    Article save(@NotNull Article article);

    Optional<Article> findArticleById(Long id);

    Page<Article> findAllByVisibilityTrue(Pageable pageable);
}
