package edu.model.db.repository;

import edu.model.db.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdOrderByCommentDateDesc(Long articleId);

    int countByArticleId(Long articleId);
}
