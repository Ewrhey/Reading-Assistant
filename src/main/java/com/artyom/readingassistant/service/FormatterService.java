package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ArticleResult;
import org.springframework.stereotype.Service;

@Service
public class FormatterService {

    public String formatArticleForTelegram(ArticleResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(result.getTitle()).append("*\n\n");

        if (!result.getSummary().isEmpty()) {
            sb.append("*Summary:*\n");
            result.getSummary().forEach(s -> sb.append("- ").append(s).append("\n"));
            sb.append("\n");
        }

        if (!result.getKeyIdeas().isEmpty()) {
            sb.append("*Key Ideas:*\n");
            for (int i = 0; i < result.getKeyIdeas().size(); i++) {
                sb.append(i + 1).append(". ").append(result.getKeyIdeas().get(i)).append("\n");
            }
            sb.append("\n");
        }

        if (!result.getActionItems().isEmpty()) {
            sb.append("*Action Items:*\n");
            result.getActionItems().forEach(a -> sb.append("- [ ] ").append(a).append("\n"));
            sb.append("\n");
        }

        return sb.toString().trim();
    }
}
