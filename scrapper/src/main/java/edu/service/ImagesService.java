package edu.service;

import edu.model.db.entity.Image;
import edu.model.db.repository.ImagesRepository;
import edu.model.db.repository.MimeTypesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImagesService {
    @Autowired
    private ImagesRepository repository;

    @Autowired
    private MimeTypesRepository mimeTypesRepository;

    public Image findImageByPath(String path) {
        return repository.findImageByPath(path).orElse(null);
    }

    public String findImageType(Image image) {
        return mimeTypesRepository.findMimeTypeById(image.getMimeTypeId()).getType();
    }
}
