package edu.service;

import edu.model.db.entity.Tag;
import edu.model.db.repository.TagsRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagsService {
    @Autowired
    private TagsRepository repository;

    public Optional<Tag> findByName(String name) {
        return repository.findTagByName(name);
    }

    public Tag findOrCreate(String name) {
        return repository.findTagByName(name)
                .orElseGet(() -> repository.save(
                        Tag.builder().name(name).build()
                ));
    }
}
