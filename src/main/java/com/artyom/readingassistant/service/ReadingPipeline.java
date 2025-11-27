package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ActionItem;
import com.artyom.readingassistant.model.ArticleResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrator for the article processing pipeline.
 * - Fetch raw article (ArticleFetcher -> ArticleResult)
 * - Preprocess text into sentences (TextPreprocessor)
 * - Produce summary (SummaryEngine)
 * - Extract key ideas (KeyIdeasExtractor)
 * - Extract action items (ActionItemsExtractor)
 * - Return consolidated ArticleResult
 */
@Service
public class ReadingPipeline {

    private final ArticleFetcher fetcher;
    private final TextPreprocessor preprocessor;
    private final SummaryEngine summaryEngine;
    private final KeyIdeasExtractor keyIdeasExtractor;
    private final ActionItemsExtractor actionItemsExtractor;

    public ReadingPipeline(
            ArticleFetcher fetcher,
            TextPreprocessor preprocessor,
            SummaryEngine summaryEngine,
            KeyIdeasExtractor keyIdeasExtractor,
            ActionItemsExtractor actionItemsExtractor
    ) {
        this.fetcher = fetcher;
        this.preprocessor = preprocessor;
        this.summaryEngine = summaryEngine;
        this.keyIdeasExtractor = keyIdeasExtractor;
        this.actionItemsExtractor = actionItemsExtractor;
    }

    /**
     * Analyze the URL and produce ArticleResult with summary/key ideas/action items.
     *
     * @param url article URL
     * @return ArticleResult containing both raw text and processed fragments
     */
    public ArticleResult analyze(String url) {
        // 1) fetch article (returns ArticleResult with title and text)
        ArticleResult fetched = fetcher.fetch(url);

        if (fetched == null) {
            // defensive: return empty result
            return new ArticleResult(url, "", "", List.of(), List.of(), List.of());
        }

        String rawText = fetched != null && fetched.getText() != null ? fetched.getText() : "";

        // 2) preprocess: split into sentences, clean up
        List<String> sentences = preprocessor.preprocess(fetched.getText());

        // 3) summary: top N sentences
        List<String> summary = summaryEngine.summarize(sentences);

        // 4) key ideas
        List<String> keyIdeas = keyIdeasExtractor.extract(sentences);

        // 5) action items (ActionItemsExtractor may return domain objects) -> map to strings
        List<ActionItem> actionItemsDomain = actionItemsExtractor.extract(fetched.getText());
        List<String> actionItems = actionItemsDomain == null
                ? List.of()
                : actionItemsDomain.stream().map(ActionItem::getText).collect(Collectors.toList());

        // 6) assemble final ArticleResult (keep original title and raw text)
        ArticleResult result = new ArticleResult();
        result.setUrl(fetched.getUrl());
        result.setTitle(fetched.getTitle());
        result.setText(fetched.getText());
        result.setSummary(summary);
        result.setKeyIdeas(keyIdeas);
        result.setActionItems(actionItems);

        return result;
    }
}
