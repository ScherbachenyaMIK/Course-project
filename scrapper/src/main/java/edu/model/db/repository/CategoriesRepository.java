package edu.model.db.repository;

import edu.model.db.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepository extends JpaRepository<Category, Long> {
    Category findCategoryById(Long id);

    Category findCategoryByName(String categoryName);
}
