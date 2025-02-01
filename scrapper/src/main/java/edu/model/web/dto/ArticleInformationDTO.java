package edu.model.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.model.web.DTO;
import java.time.ZonedDateTime;

public record ArticleInformationDTO(
        String tags,
        String categories,
        int timeToRead,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
        ZonedDateTime creationDate,
        String status,
        int views,
        int likes,
        int comments
) implements DTO {
}
