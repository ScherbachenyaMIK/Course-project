package edu.service;

import edu.exception.UnsupportedMediaTypeException;
import edu.model.db.entity.Image;
import edu.model.db.entity.MimeType;
import edu.model.db.repository.ImagesRepository;
import edu.model.db.repository.MimeTypesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public MimeType validateAndGetMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new UnsupportedMediaTypeException("MIME type is missing");
        }
        return mimeTypesRepository.findByType(contentType.toLowerCase())
                .orElseThrow(() -> new UnsupportedMediaTypeException(
                        "Unsupported MIME type: " + contentType));
    }

    @Transactional
    public Image saveImage(String path, String filename, MimeType mimeType, byte[] content) {
        Image image = Image.builder()
                .path(path)
                .filename(filename)
                .mimeTypeId(mimeType.getId().intValue())
                .content(content)
                .build();
        return repository.save(image);
    }

    @Transactional
    public Image replaceImage(String path, String filename, MimeType mimeType, byte[] content) {
        repository.deleteByPath(path);
        return saveImage(path, filename, mimeType, content);
    }

    @Transactional
    public Image saveArticleImage(Long articleId, String filename, MimeType mimeType, byte[] content) {
        String basePath = "/article/" + articleId + "/images/";
        // First insert with placeholder path to obtain the generated id, then patch it in.
        Image image = Image.builder()
                .path(basePath + "pending")
                .filename(filename)
                .mimeTypeId(mimeType.getId().intValue())
                .content(content)
                .build();
        Image persisted = repository.save(image);
        persisted.setPath(basePath + persisted.getId());
        return repository.save(persisted);
    }
}
