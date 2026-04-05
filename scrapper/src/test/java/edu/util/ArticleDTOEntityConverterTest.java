package edu.util;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Comment;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.dto.ArticleDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleDTOEntityConverterTest {

    private User sampleUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private Article sampleArticle() {
        User author = sampleUser(7L, "alice");
        Tag tag = Tag.builder().id(1L).name("java").build();
        Category category = Category.builder().id(2L).name("tech").build();
        return Article.builder()
                .id(100L)
                .author(author)
                .title("Title")
                .textContent("Body")
                .timeToRead(5)
                .lastUpdateDate(LocalDateTime.of(2026, 1, 1, 12, 0))
                .visibility(true)
                .status("published")
                .views(3)
                .likes(2)
                .tags(Set.of(tag))
                .categories(Set.of(category))
                .build();
    }

    @Test
    void convertWithoutComments() {
        Article article = sampleArticle();

        ArticleDTO dto = ArticleDTOEntityConverter.convert(article, null);

        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.content()).isEqualTo("Body");
        assertThat(dto.author()).isEqualTo("alice");
        assertThat(dto.comments()).isEmpty();
        assertThat(dto.authorIconUri().toString()).isEqualTo("/resources/user_icon/7");
        assertThat(dto.information().likes()).isEqualTo(2);
        assertThat(dto.information().views()).isEqualTo(3);
        assertThat(dto.information().comments()).isZero();
        assertThat(dto.information().timeToRead()).isEqualTo(5);
        assertThat(dto.information().status()).isEqualTo("published");
        assertThat(dto.information().tags()).isEqualTo("java");
        assertThat(dto.information().categories()).isEqualTo("tech");
    }

    @Test
    void convertWithComments() {
        Article article = sampleArticle();
        User commenter = sampleUser(9L, "bob");
        Comment comment = Comment.builder()
                .id(1L)
                .user(commenter)
                .commentText("nice")
                .commentDate(LocalDateTime.of(2026, 2, 1, 10, 0))
                .build();

        ArticleDTO dto = ArticleDTOEntityConverter.convert(article, List.of(comment));

        assertThat(dto.comments()).hasSize(1);
        assertThat(dto.comments().get(0).author()).isEqualTo("bob");
        assertThat(dto.comments().get(0).text()).isEqualTo("nice");
        assertThat(dto.comments().get(0).authorIconUri().toString()).isEqualTo("/resources/user_icon/9");
        assertThat(dto.information().comments()).isEqualTo(1);
    }

    @Test
    void emptyDTOHasNoFields() {
        ArticleDTO dto = ArticleDTOEntityConverter.emptyDTO();

        assertThat(dto.title()).isNull();
        assertThat(dto.content()).isNull();
        assertThat(dto.author()).isNull();
        assertThat(dto.information()).isNull();
    }
}
