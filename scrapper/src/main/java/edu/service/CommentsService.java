package edu.service;

import edu.model.db.entity.Comment;
import edu.model.db.repository.CommentsRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {
    @Autowired
    private CommentsRepository repository;

    public Comment save(Comment comment) {
        return repository.save(comment);
    }

    public List<Comment> getArticleComments(Long articleId) {
        return repository.findByArticleIdOrderByCommentDateDesc(articleId);
    }

    public int countByArticle(Long articleId) {
        return repository.countByArticleId(articleId);
    }
}
