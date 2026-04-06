package edu.model.web.dto;

import edu.model.web.DTO;
import java.util.List;

public record CategoriesDTO(
        List<CategoryItemDTO> categories
) implements DTO {
}
