package edu.service;

import edu.PostgreIntegrationTest;
import edu.cofiguration.NoKafkaConfig;
import edu.model.db.entity.Article;
import edu.model.db.entity.User;
import edu.model.db.repository.ArticlesRepository;
import edu.model.db.repository.UsersRepository;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(NoKafkaConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArticlesServiceTest extends PostgreIntegrationTest {
    @Autowired
    private ArticlesService service;

    @Autowired
    private ArticlesRepository repository;

    private static final User user = User.builder()
            .username("testUsername")
            .name("testName")
            .email("test@mail.by")
            .passwordHash("VerySecretPassword")
            .userRole("USER")
            .sex('M')
            .birthDate(new Timestamp(954882000).toLocalDateTime())
            .build();

    @BeforeAll
    static void init(@Autowired UsersRepository usersRepository) {
        usersRepository.save(user);
    }

    @AfterAll
    static void finalize(@Autowired UsersRepository usersRepository) {
        usersRepository.delete(user);
    }

    private Article fArticle = Article.builder()
            .author(user)
            .id(1L)
            .title("title")
            .textContent("content")
            .visibility(true)
            .build();
    private final Article sArticle = Article.builder()
            .author(user)
            .id(2L)
            .title("title2")
            .textContent("content2")
            .visibility(false)
            .build();
    private Article tArticle = Article.builder()
            .author(user)
            .id(3L)
            .title("title3")
            .textContent("content3")
            .visibility(true)
            .build();

    @Test
    @Order(1)
    void setupNewArticle() {
        assertThat(service.setupArticle(fArticle))
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author"
                )
                .isEqualTo(fArticle);
    }

    @Test
    @Order(2)
    void setupSecondArticle() {
        assertThat(service.setupArticle(sArticle))
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author"
                )
                .isEqualTo(sArticle);
    }

    @Test
    @Order(3)
    void findArticleById() {
        assertThat(service.getArticle(1L))
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author",
                        "tags",
                        "categories"
                )
                .isEqualTo(fArticle);
    }

    @Test
    @Order(4)
    void findNotVisibleArticleById() {
        assertThat(service.getArticle(2L))
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author",
                        "tags",
                        "categories"
                )
                .isEqualTo(sArticle);
    }

    @Test
    @Order(5)
    void setupThirdArticle() {
        assertThat(service.setupArticle(tArticle))
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author"
                )
                .isEqualTo(tArticle);
    }

    @Test
    @Order(6)
    void findVisibleArticlesSlice() {
        fArticle = repository.findArticleById(fArticle.getId()).orElse(fArticle);
        tArticle = repository.findArticleById(tArticle.getId()).orElse(tArticle);
        fArticle.setVisibility(true);
        tArticle.setVisibility(true);
        repository.save(fArticle);
        repository.save(tArticle);

        List<Article> actual = service.getArticlesSlice(3);

        assertThat(actual.getFirst())
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author",
                        "tags",
                        "categories"
                )
                .isEqualTo(fArticle);

        assertThat(actual.getLast())
                .usingRecursiveComparison()
                .ignoringFields(
                        "likes",
                        "views",
                        "status",
                        "visibility",
                        "lastUpdateDate",
                        "creationDate",
                        "timeToRead",
                        "author",
                        "tags",
                        "categories"
                )
                .isEqualTo(tArticle);
    }
}