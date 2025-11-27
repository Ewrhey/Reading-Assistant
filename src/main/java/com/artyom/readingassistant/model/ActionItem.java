package com.artyom.readingassistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single actionable instruction extracted from the article.
 * This model is used to store individual sentences that indicate
 * recommended actions, tasks, or next steps found in the text.
 */
@Data
@NoArgsConstructor      // Required for JSON serialization/deserialization
@AllArgsConstructor     // Allows creating ActionItem(text) in ActionItemsExtractor
public class ActionItem {

    /**
     * The extracted action sentence. This is typically a line containing
     * verbs like "нужно", "следует", "рекомендуется" or other markers
     * that indicate a recommended next step.
     */
    private String text;
}
