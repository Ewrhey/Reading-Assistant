package com.artyom.readingassistant.model;

import lombok.*;

import java.util.List;

/**
 * DTO for API responses. Holds both raw text and processed parts (summary, key ideas, action items).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ArticleResult {
    private String url;
    private String title;
    private String text; // raw cleaned text
    private List<String> summary; // top sentences
    private List<String> keyIdeas;
    private List<String> actionItems; // simple string form of action items

    public ArticleResult(String url, String title, String text) {
        this.url = url;
        this.title = title;
        this.text = text;
    }

}
