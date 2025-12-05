package edu.model.web.dto;

import java.util.List;

public record UserDTO(
        Long id,
        String username,
        String nativeName,
        String email,
        String registrationDate,
        String description,
        String role,
        Character sex,
        String birthDate,
        List<ArticlePreviewDTO> articles
) {
}
