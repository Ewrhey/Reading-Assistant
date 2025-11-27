package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ArticleResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormatForPdfService {

    public String formatPlainText(ArticleResult r) {
        StringBuilder sb = new StringBuilder();

        sb.append(r.getTitle() != null ? r.getTitle() : "No title").append("\n");
        sb.append("URL: ").append(r.getUrl() != null ? r.getUrl() : "").append("\n\n");

        sb.append("---- Summary ----\n");
        List<String> summary = safeList(r.getSummary());
        if (summary.isEmpty()) {
            sb.append("(no summary)\n\n");
        } else {
            summary.forEach(s -> sb.append("• ").append(s).append("\n"));
            sb.append("\n");
        }

        sb.append("---- Key ideas ----\n");
        List<String> ideas = safeList(r.getKeyIdeas());
        if (ideas.isEmpty()) {
            sb.append("(none)\n\n");
        } else {
            for (int i = 0; i < ideas.size(); i++) {
                sb.append(i + 1).append(". ").append(ideas.get(i)).append("\n");
            }
            sb.append("\n");
        }

        sb.append("---- Action items ----\n");
        List<String> items = safeList(r.getActionItems());
        if (items.isEmpty()) {
            sb.append("(none)\n\n");
        } else {
            items.forEach(it -> sb.append("[ ] ").append(it).append("\n"));
            sb.append("\n");
        }

        sb.append("---- Full text (snippet) ----\n");
        if (r.getText() != null && !r.getText().isBlank()) {
            String snippet = r.getText().length() > 4000 ? r.getText().substring(0, 4000) + "..." : r.getText();
            sb.append(snippet).append("\n");
        } else {
            sb.append("(no text)\n");
        }

        return sb.toString();
    }

    private List<String> safeList(List<String> list) {
        return list == null ? List.of() : list.stream().collect(Collectors.toList());
    }
}
