package edu.model.db.repository;

import edu.model.db.entity.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagesRepository extends JpaRepository<Image, Long> {
    Optional<Image> findImageByPath(String path);
}
