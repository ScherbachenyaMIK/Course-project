package edu.model.db.repository;

import edu.model.db.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends JpaRepository<Tag, Long> {
    Tag findTagById(Long id);

    Optional<Tag> findTagByName(String categoryName);
}
