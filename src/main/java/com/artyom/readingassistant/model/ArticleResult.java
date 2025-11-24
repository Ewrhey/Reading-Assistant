package com.artyom.readingassistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO that holds the main pieces returned by the fetcher.
 * It can be extended later with more fields (e.g. language, wordCount).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResult {
    private String url;
    private String title;
    private String text;

}
