package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ActionItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ActionItemsExtractor {

    // Unicode-aware lookarounds, DOTALL not needed because we check per-line/sentence
    private static final Pattern ACTION_PATTERN_PER_SENTENCE = Pattern.compile(
            "(?i)(?<!\\p{L})(нужно|нужн[а-я]*|следует|следуетс[я]?|рекомендуется|рекомендуем[ая-я]*|обязательно|стоит|важно|желательно|требуется|необходимо|советуем|советуется|можно сделать|можно выполнить|полезно|правильно будет|лучше всего|теперь нужно|теперь следует|теперь рекомендуется|теперь необходимо)(?!\\p{L})",
            Pattern.UNICODE_CASE
    );

    public List<ActionItem> extract(String text) {
        List<ActionItem> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        // Разбиваем по параграфам/строкам — это простая и устойчивая стратегия.
        String[] lines = text.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            Matcher matcher = ACTION_PATTERN_PER_SENTENCE.matcher(line);
            if (matcher.find()) {
                // можно дополнительно нормализовать line, убрав лишние пробелы
                results.add(new ActionItem(line));
            }
        }
        return results;
    }
}
