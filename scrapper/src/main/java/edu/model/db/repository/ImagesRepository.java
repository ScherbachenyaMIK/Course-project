package edu.model.db.repository;

import edu.model.db.entity.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImagesRepository extends JpaRepository<Image, Long> {
    Optional<Image> findImageByPath(String path);

    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.path = :path")
    int deleteByPath(String path);
}
