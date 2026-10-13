package com.smarttrex.backend;

public record CatalogBook(
        long id,
        String title,
        String author,
        String language,
        String downloadUrl,
        String coverUrl
) {

}
