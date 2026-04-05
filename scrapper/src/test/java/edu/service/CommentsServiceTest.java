package edu.service;

import edu.model.db.entity.Comment;
import edu.model.db.repository.CommentsRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CommentsServiceTest {
    @Mock
    private CommentsRepository repository;

    @InjectMocks
    private CommentsService commentsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveDelegatesToRepository() {
        Comment comment = Comment.builder().commentText("hi").build();
        Comment persisted = Comment.builder().id(5L).commentText("hi").build();
        when(repository.save(comment)).thenReturn(persisted);

        Comment result = commentsService.save(comment);

        assertThat(result).isSameAs(persisted);
    }

    @Test
    void getArticleCommentsReturnsOrderedList() {
        Comment c1 = Comment.builder().id(1L).commentText("a").build();
        Comment c2 = Comment.builder().id(2L).commentText("b").build();
        when(repository.findByArticleIdOrderByCommentDateDesc(10L)).thenReturn(List.of(c1, c2));

        List<Comment> result = commentsService.getArticleComments(10L);

        assertThat(result).containsExactly(c1, c2);
    }

    @Test
    void countByArticleReturnsRepositoryValue() {
        when(repository.countByArticleId(42L)).thenReturn(7);

        assertThat(commentsService.countByArticle(42L)).isEqualTo(7);
    }
}
