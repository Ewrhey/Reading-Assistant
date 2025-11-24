package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ArticleResult;
import org.springframework.stereotype.Component;

/**
 * Orchestrates the reading pipeline.
 * For Day 1 it simply delegates to ArticleFetcher.
 * Later this class will call TextPreprocessor, SummaryEngine, etc.
 */
@Component
public class ReadingPipeline {

    private final ArticleFetcher fetcher;

    public ReadingPipeline(ArticleFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Execute full pipeline for given url.
     * Currently this method returns raw fetched text.
     *
     * @param url source article url
     * @return ArticleResult containing url, title and extracted text
     */
    public ArticleResult analyze(String url) {
        // For Day 1 we only fetch and return text.
        return fetcher.fetch(url);
    }
}
