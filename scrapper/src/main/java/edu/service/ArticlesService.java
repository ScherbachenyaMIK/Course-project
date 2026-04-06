package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.repository.ArticlesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ArticlesService {
    @Autowired
    private ArticlesRepository repository;

    public Article setupArticle(Article article) {
        return repository.save(article);
    }

    public Article getArticle(Long id) {
        return repository.findArticleById(id).orElse(null);
    }

    public List<Article> getArticlesSlice(int count) {
        return repository.findAllByVisibilityTrue(PageRequest.of(0, count)).stream().toList();
    }

    public void incrementViews(Long articleId) {
        repository.incrementViews(articleId);
    }

    public void incrementLikes(Long articleId) {
        repository.incrementLikes(articleId);
    }

    @SuppressWarnings("ParameterNumber")
    public List<Article> searchArticles(String query, int minLikes, int minViews,
                                        int minComments, List<String> tags,
                                        List<String> categories, String sort, int limit) {
        List<String> safeTags = tags == null ? List.of() : tags;
        List<String> safeCategories = categories == null ? List.of() : categories;
        return repository.searchArticles(
                query == null ? "" : query.trim(),
                minLikes, minViews, minComments,
                safeTags.isEmpty() ? List.of("") : safeTags,
                safeTags.isEmpty(),
                safeTags.size(),
                safeCategories.isEmpty() ? List.of("") : safeCategories,
                safeCategories.isEmpty(),
                safeCategories.size(),
                sort == null ? "" : sort,
                limit);
    }
}
