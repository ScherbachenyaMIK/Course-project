package edu.model.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.model.web.DTO;
import java.net.URI;
import java.time.ZonedDateTime;
import lombok.Builder;

@Builder
public record CommentDTO(
        String author,
        URI authorIconUri,
        String text,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
        ZonedDateTime date
) implements DTO {
}
