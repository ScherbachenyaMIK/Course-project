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
}
