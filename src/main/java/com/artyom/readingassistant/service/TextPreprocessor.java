package com.artyom.readingassistant.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextPreprocessor {

    /**
     * Полная предобработка текста:
     * 1) Нормализация пробелов
     * 2) Разбиение на предложения
     * 3) Фильтрация коротких и пустых предложений
     */
    public List<String> preprocess(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }

        String cleaned = normalize(rawText);
        List<String> sentences = splitIntoSentences(cleaned);

        List<String> result = new ArrayList<>();
        for (String s : sentences) {
            String trimmed = s.trim();
            if (trimmed.length() >= 30 && trimmed.split("\\s+").length >= 3) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private String normalize(String text) {
        return text
                .replaceAll("\\s+", " ")
                .replaceAll("\n", " ")
                .trim();
    }

    private List<String> splitIntoSentences(String text) {
        List<String> result = new ArrayList<>();

        String[] parts = text.split("(?<=[.!?])\\s+");
        for (String p : parts) {
            if (!p.isBlank()) result.add(p.trim());
        }

        return result;
    }
}
