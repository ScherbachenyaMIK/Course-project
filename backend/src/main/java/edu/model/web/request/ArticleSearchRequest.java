package edu.model.web.request;

import edu.model.web.ScrapperGetRequest;
import java.util.List;

public record ArticleSearchRequest(
        String query,
        Integer minLikes,
        Integer minViews,
        Integer minComments,
        List<String> tags,
        List<String> categories,
        String sort,
        int limit
) implements ScrapperGetRequest {
}
