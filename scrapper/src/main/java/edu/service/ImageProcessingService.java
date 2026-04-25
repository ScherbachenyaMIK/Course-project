package edu.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {
    private static final Logger LOGGER = LogManager.getLogger(ImageProcessingService.class);

    @Async("imageProcessingExecutor")
    public void processAsync(Long imageId, String path, String mimeType) {
        LOGGER.info("Started async image processing: id={}, path={}, mimeType={}",
                imageId, path, mimeType);
        // Placeholder: actual optimization (resize, compress) would be performed here.
        LOGGER.info("Finished async image processing: id={}", imageId);
    }
}
