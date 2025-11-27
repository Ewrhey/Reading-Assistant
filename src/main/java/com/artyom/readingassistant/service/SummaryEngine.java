package com.artyom.readingassistant.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Improved rule-based summarizer.
 *
 * Key ideas:
 * - normalize sentences (trim, collapse spaces)
 * - filter out obvious code/html/noise lines
 * - compute score: length + keyword bonus + position bonus + frequency bonus
 * - select top N by score, then return them in original order (to keep coherence)
 */
@Service
public class SummaryEngine {

    private static final Set<String> KEYWORDS = Set.of(
            "важно", "ключ", "главное", "основной", "результат", "итог", "вывод", "рекомендуется", "нужно"
    );

    // configurable params
    private static final int MAX_SENTENCES = 5;
    private static final int MIN_LENGTH = 20; // ignore too short lines after normalization

    public List<String> summarize(List<String> sentences) {
        if (sentences == null || sentences.isEmpty()) return List.of();

        // 1) Normalize and filter, keep original index
        List<Sentence> cleaned = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String raw = sentences.get(i);
            if (raw == null) continue;
            String normalized = normalize(raw);
            if (normalized.length() < MIN_LENGTH) continue;
            if (isNoisy(normalized)) continue;
            cleaned.add(new Sentence(i, raw, normalized));
        }

        if (cleaned.isEmpty()) return List.of();

        // 2) Frequency map of normalized sentences (to give bonus for repeats)
        Map<String, Integer> freq = new HashMap<>();
        for (Sentence s : cleaned) {
            freq.put(s.normalized, freq.getOrDefault(s.normalized, 0) + 1);
        }

        // 3) Score each sentence
        for (Sentence s : cleaned) {
            int score = 0;
            score += lengthScore(s.normalized);
            score += keywordScore(s.normalized);
            score += positionScore(s.index, sentences.size());
            score += freq.getOrDefault(s.normalized, 0) - 1; // bonus if repeated
            s.score = score;
        }

        // 4) Select top-K by score (stable: if equal scores, prefer lower index)
        List<Sentence> top = cleaned.stream()
                .sorted(Comparator.comparingInt((Sentence s) -> -s.score).thenComparingInt(s -> s.index))
                .limit(MAX_SENTENCES)
                .collect(Collectors.toList());

        // 5) Return top sentences sorted by original position (to keep coherence)
        top.sort(Comparator.comparingInt(s -> s.index));
        return top.stream().map(s -> s.original).collect(Collectors.toList());
    }

    // Normalize: trim, collapse whitespace, remove leading/trailing punctuation
    private String normalize(String s) {
        String t = s.replaceAll("\\s+", " ").trim();
        // remove surrounding quotes or dashes
        t = t.replaceAll("^[-—:\\s]+", "");
        t = t.replaceAll("[-—:\\s]+$", "");
        return t;
    }

    // Heuristic to detect code/html/snippets
    private boolean isNoisy(String s) {
        String lower = s.toLowerCase();
        if (lower.contains("<") && lower.contains(">")) return true; // markup
        if (lower.contains("<?xml") || lower.contains("</") || lower.contains("/>")) return true;
        if (lower.matches(".*\\b(http|https)://.*")) return true; // url
        if (lower.matches(".*\\{\\s*\\w+.*") || lower.matches(".*;\\s*$")) return true; // likely code
        // long lists of repeated words or punctuation
        if (s.length() > 1000) return true;
        return false;
    }

    private int lengthScore(String s) {
        int len = s.length();
        // prefer medium-length informative sentences (not tiny, not huge)
        if (len < 40) return 0;
        if (len < 120) return Math.min(6, len / 20); // 2..6
        return 6 + Math.min(4, (len - 120) / 50); // small bonus for very long
    }

    private int keywordScore(String s) {
        int bonus = 0;
        String lower = s.toLowerCase();
        for (String k : KEYWORDS) {
            if (lower.contains(k)) bonus += 4;
        }
        return bonus;
    }

    // position score: beginning and end paragraphs are often important
    private int positionScore(int index, int total) {
        if (index < 2) return 3; // first two sentences bonus
        if (index >= total - 2) return 2; // last two sentences
        return 0;
    }

    // small helper class
    private static class Sentence {
        final int index;
        final String original;
        final String normalized;
        int score;

        Sentence(int index, String original, String normalized) {
            this.index = index;
            this.original = original;
            this.normalized = normalized;
            this.score = 0;
        }
    }
}
