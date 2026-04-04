package edu.service;

import edu.model.db.entity.Tag;
import edu.model.db.repository.TagsRepository;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TagsServiceTest {
    @Mock
    private TagsRepository repository;

    @InjectMocks
    private TagsService tagsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findByNameExisting() {
        Tag tag = new Tag(1L, "java", new HashSet<>());
        when(repository.findTagByName("java")).thenReturn(Optional.of(tag));

        Optional<Tag> result = tagsService.findByName("java");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("java");
    }

    @Test
    void findByNameNotFound() {
        when(repository.findTagByName("unknown")).thenReturn(Optional.empty());

        Optional<Tag> result = tagsService.findByName("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void findOrCreateExisting() {
        Tag existing = new Tag(1L, "java", new HashSet<>());
        when(repository.findTagByName("java")).thenReturn(Optional.of(existing));

        Tag result = tagsService.findOrCreate("java");

        assertThat(result.getName()).isEqualTo("java");
        verify(repository, never()).save(any());
    }

    @Test
    void findOrCreateNew() {
        Tag saved = new Tag(2L, "spring", new HashSet<>());
        when(repository.findTagByName("spring")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(saved);

        Tag result = tagsService.findOrCreate("spring");

        assertThat(result.getName()).isEqualTo("spring");
        verify(repository).save(any());
    }
}
