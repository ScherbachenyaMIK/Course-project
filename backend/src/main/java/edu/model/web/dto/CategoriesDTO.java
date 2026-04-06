package edu.model.web.dto;

import java.util.List;

public record CategoriesDTO(
        List<CategoryItemDTO> categories
) {
}
