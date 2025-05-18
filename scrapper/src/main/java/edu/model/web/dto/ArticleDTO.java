package edu.model.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.model.web.DTO;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import lombok.Builder;

@Builder
public record ArticleDTO(
        URI authorIconUri,
        String author,
        String title,
        String content,
        ArticleInformationDTO information,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
        ZonedDateTime lastUpdateDate,
        ArrayList<String> comments
) implements DTO {
}
