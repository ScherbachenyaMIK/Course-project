package edu.model.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.model.web.DTO;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record UserDTO(
    Long id,
    String username,
    String nativeName,
    String email,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    ZonedDateTime registrationDate,
    String description,
    String role,
    Character sex,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
    ZonedDateTime birthDate,
    List<ArticlePreviewDTO> articles
) implements DTO {
}
