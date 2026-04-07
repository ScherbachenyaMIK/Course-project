package edu.util;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.dto.ArticlePreviewDTO;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticlePreviewDTOEntityConverterTest {

    private static final User user = User.builder()
            .id(1L)
            .username("testUser")
            .name("Test")
            .email("test@mail.com")
            .userRole("USER")
            .build();

    @Test
    void convertSingleArticle() {
        Article article = Article.builder()
                .id(1L)
                .author(user)
                .title("Test Title")
                .textContent("This is a test article content that is long enough to test.")
                .visibility(true)
                .likes(10)
                .views(100)
                .timeToRead(5)
                .status("published")
                .lastUpdateDate(LocalDateTime.of(2025, 5, 5, 10, 0))
                .creationDate(LocalDateTime.of(2025, 5, 1, 10, 0))
                .tags(Set.of(
                        new Tag(1L, "java", new HashSet<>()),
                        new Tag(2L, "spring", new HashSet<>())
                ))
                .categories(Set.of(
                        new Category(1L, "Programming", "About coding", new HashSet<>())
                ))
                .build();

        List<ArticlePreviewDTO> result =
                ArticlePreviewDTOEntityConverter.convert(List.of(article));

        assertThat(result).hasSize(1);
        ArticlePreviewDTO dto = result.getFirst();
        assertThat(dto.title()).isEqualTo("Test Title");
        assertThat(dto.author()).isEqualTo("testUser");
        assertThat(dto.information().likes()).isEqualTo(10);
        assertThat(dto.information().views()).isEqualTo(100);
        assertThat(dto.information().timeToRead()).isEqualTo(5);
        assertThat(dto.information().categories()).isEqualTo("Programming");
        assertThat(dto.articleUri().toString()).isEqualTo("/articles/1");
        assertThat(dto.authorIconUri().toString()).isEqualTo("/resources/user_icon/1");
        assertThat(dto.previewImageUri().toString()).isEqualTo("/resources/preview/1");
    }

    @Test
    void convertEmptyList() {
        List<ArticlePreviewDTO> result =
                ArticlePreviewDTOEntityConverter.convert(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void convertMultipleArticles() {
        Article article1 = Article.builder()
                .id(1L)
                .author(user)
                .title("First")
                .textContent("Content of the first article for testing purposes here.")
                .visibility(true)
                .likes(5)
                .views(50)
                .timeToRead(3)
                .status("published")
                .lastUpdateDate(LocalDateTime.now())
                .creationDate(LocalDateTime.now())
                .tags(new HashSet<>())
                .categories(new HashSet<>())
                .build();
        Article article2 = Article.builder()
                .id(2L)
                .author(user)
                .title("Second")
                .textContent("Content of the second article for testing purposes here.")
                .visibility(true)
                .likes(15)
                .views(150)
                .timeToRead(7)
                .status("draft")
                .lastUpdateDate(LocalDateTime.now())
                .creationDate(LocalDateTime.now())
                .tags(new HashSet<>())
                .categories(new HashSet<>())
                .build();

        List<ArticlePreviewDTO> result =
                ArticlePreviewDTOEntityConverter.convert(List.of(article1, article2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("First");
        assertThat(result.get(1).title()).isEqualTo("Second");
    }

    @Test
    void convertArticleWithEmptyTagsAndCategories() {
        Article article = Article.builder()
                .id(3L)
                .author(user)
                .title("No Tags")
                .textContent("Article with no tags and no categories content here.")
                .visibility(true)
                .likes(0)
                .views(0)
                .timeToRead(1)
                .status("draft")
                .lastUpdateDate(LocalDateTime.now())
                .creationDate(LocalDateTime.now())
                .tags(new HashSet<>())
                .categories(new HashSet<>())
                .build();

        List<ArticlePreviewDTO> result =
                ArticlePreviewDTOEntityConverter.convert(List.of(article));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().information().tags()).isEmpty();
        assertThat(result.getFirst().information().categories()).isEmpty();
    }
}
