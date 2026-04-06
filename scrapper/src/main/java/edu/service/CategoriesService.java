package edu.service;

import edu.model.db.entity.Category;
import edu.model.db.repository.CategoriesRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoriesService {
    @Autowired
    private CategoriesRepository repository;

    public Category findByName(String name) {
        return repository.findCategoryByName(name);
    }

    public List<Category> findAllSorted() {
        return repository.findAllByOrderByNameAsc();
    }
}
