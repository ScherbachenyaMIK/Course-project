package edu.service;

import edu.model.db.entity.Category;
import edu.model.db.repository.CategoriesRepository;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CategoriesServiceTest {
    @Mock
    private CategoriesRepository repository;

    @InjectMocks
    private CategoriesService categoriesService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByNameExisting() {
        Category category = new Category(1L, "Tech", "Technology", new HashSet<>());
        when(repository.findCategoryByName("Tech")).thenReturn(category);

        Category result = categoriesService.findByName("Tech");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tech");
    }

    @Test
    void findByNameNotFound() {
        when(repository.findCategoryByName("Unknown")).thenReturn(null);

        Category result = categoriesService.findByName("Unknown");

        assertThat(result).isNull();
    }

    @Test
    void findAllSorted() {
        List<Category> categories = List.of(
                new Category(1L, "Art", "Creative works", new HashSet<>()),
                new Category(2L, "Biology", "Living organisms", new HashSet<>()),
                new Category(3L, "Tech", "Technology", new HashSet<>())
        );
        when(repository.findAllByOrderByNameAsc()).thenReturn(categories);

        List<Category> result = categoriesService.findAllSorted();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Art");
        assertThat(result.get(2).getName()).isEqualTo("Tech");
    }

    @Test
    void findAllSortedEmpty() {
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<Category> result = categoriesService.findAllSorted();

        assertThat(result).isEmpty();
    }
}
