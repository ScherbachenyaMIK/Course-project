package edu.web;

import lombok.Getter;

@Getter
public class ScrapperUploadException extends RuntimeException {
    private final int statusCode;

    public ScrapperUploadException(int statusCode, String body) {
        super("Scrapper upload failed (" + statusCode + "): " + body);
        this.statusCode = statusCode;
    }
}
