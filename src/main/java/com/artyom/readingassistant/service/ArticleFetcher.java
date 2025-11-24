package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ArticleResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Fetches an article from the given URL and extracts a clean textual representation.
 *
 * Responsibilities:
 *  - Download the page (with a small timeout)
 *  - Try to locate the main article element (several heuristics)
 *  - Fallback to body text if article element not found
 *  - Return ArticleResult containing URL, title and extracted text
 *
 * This component is intentionally simple and robust: it tolerates missing elements,
 * performs safe fallbacks and returns reasonable defaults instead of throwing for minor problems.
 */
@Component
public class ArticleFetcher {

    private static final Logger log = LoggerFactory.getLogger(ArticleFetcher.class);

    // configurable defaults (can be moved to application.properties later)
    private static final int TIMEOUT_MILLIS = (int) Duration.ofSeconds(10).toMillis();
    private static final String USER_AGENT = "ReadingAssistantBot/1.0 (+https://example.com)";

    /**
     * Fetch article and extract main textual content.
     *
     * @param url article URL
     * @return ArticleResult with url, title and cleaned text
     * @throws IllegalArgumentException if URL is invalid or null
     */
    public ArticleResult fetch(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must be provided");
        }

        try {
            // Validate URI (throws if invalid)
            URI.create(url);

            log.info("Fetching URL: {}", url);

            Connection connection = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MILLIS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true);

            Document doc = connection.get();

            // Title extraction: prefer <title>, then og:title meta
            String title = extractTitle(doc).orElse("(no title)");

            // Try several heuristics to find the main article node
            String text = extractMainText(doc);

            // Trim and normalize whitespace
            text = normalizeText(text);

            return new ArticleResult(url, title, text);
        } catch (Exception e) {
            log.warn("Failed to fetch or parse URL {}: {}", url, e.toString());
            // On fetch error, return empty ArticleResult but keep URL to allow caller to log
            return new ArticleResult(url, "", "");
        }
    }

    // Attempt to extract title using common patterns
    private Optional<String> extractTitle(Document doc) {
        try {
            String title = doc.title();
            if (title != null && !title.isBlank()) {
                return Optional.of(title.trim());
            }

            // og:title meta
            Element og = doc.selectFirst("meta[property=og:title]");
            if (og != null) {
                String content = og.attr("content");
                if (content != null && !content.isBlank()) {
                    return Optional.of(content.trim());
                }
            }
        } catch (Exception ex) {
            log.debug("Title extraction failed: {}", ex.toString());
        }
        return Optional.empty();
    }

    // Heuristics to find main article text; tries a few selectors in order of preference
    private String extractMainText(Document doc) {
        // 1) <article>
        Element article = doc.selectFirst("article");
        if (article != null && !article.text().isBlank()) {
            return extractTextFromElement(article);
        }

        // 2) Common main selectors: <main>, common classes
        Element main = doc.selectFirst("main");
        if (main != null && !main.text().isBlank()) {
            return extractTextFromElement(main);
        }

        // 3) Common article containers by class/id heuristics
        List<String> containerSelectors = List.of(
                "div[class*=\"article\"]",
                "div[class*=\"post\"]",
                "div[id*=\"article\"]",
                "div[id*=\"post\"]",
                "div[class*=\"content\"]",
                "section[class*=\"content\"]"
        );

        for (String sel : containerSelectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) {
                return extractTextFromElement(el);
            }
        }

        // 4) Fallback to body text
        Element body = doc.body();
        if (body != null) {
            return extractTextFromElement(body);
        }

        // 5) As a last resort, return empty string
        return "";
    }

    // Extract text from an element, optionally filtering short noisy nodes and joining paragraphs.
    private String extractTextFromElement(Element el) {
        // Gather paragraphs and significant text blocks
        List<String> paragraphs = el.select("p, h1, h2, h3, li")
                .stream()
                .map(Element::text)
                .filter(s -> s != null && s.length() > 20) // filter very short lines
                .collect(Collectors.toList());

        if (!paragraphs.isEmpty()) {
            return String.join("\n\n", paragraphs);
        }

        // If no paragraphs, fallback to element.text()
        return el.text();
    }

    // Normalize whitespace, remove repeated empty lines
    private String normalizeText(String raw) {
        if (raw == null) return "";
        // replace multiple whitespace with single space, keep paragraph breaks
        String normalized = raw
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\n{3,}", "\n\n") // collapse many newlines
                .trim();
        return normalized;
    }
}
