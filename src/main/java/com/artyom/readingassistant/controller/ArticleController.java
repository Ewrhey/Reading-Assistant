package com.artyom.readingassistant.controller;

import com.artyom.readingassistant.model.ArticleResult;
import com.artyom.readingassistant.service.ReadingPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Simple REST controller that exposes the analyze API.
 * Example: GET /api/analyze?url=https://habr.com/...
 */
@RestController
@RequestMapping("/api")
public class ArticleController {

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    private final ReadingPipeline pipeline;

    public ArticleController(ReadingPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @GetMapping("/analyze")
    public ResponseEntity<ArticleResult> analyze(@RequestParam("url") String url) {
        log.info("Received analyze request for URL: {}", url);
        ArticleResult result = pipeline.analyze(url);
        return ResponseEntity.ok(result);
    }
}
