package edu.model.web.dto;

import java.net.URI;
import java.util.ArrayList;

public record ArticleDTO(
        URI authorIconUri,
        String author,
        String title,
        String content,
        ArticleInformationDTO information,
        String lastUpdateDate,
        ArrayList<String> comments
) {
}
