package edu.model.web.dto;

import java.net.URI;

public record CommentDTO(
        String author,
        URI authorIconUri,
        String text,
        String date
) {
}
