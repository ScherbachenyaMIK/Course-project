package edu.service;

import edu.model.db.entity.Category;
import edu.model.db.repository.CategoriesRepository;
import java.util.HashSet;
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
}
