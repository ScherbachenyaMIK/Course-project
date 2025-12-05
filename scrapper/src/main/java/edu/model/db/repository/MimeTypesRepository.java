package edu.model.db.repository;

import edu.model.db.entity.MimeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MimeTypesRepository extends JpaRepository<MimeType, Long> {
    MimeType findMimeTypeById(Integer id);
}
