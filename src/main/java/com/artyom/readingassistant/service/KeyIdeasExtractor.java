package com.artyom.readingassistant.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyIdeasExtractor {

    private static final String[] MARKERS = {
            "ключевая идея",
            "ключевая мысль",
            "главная идея",
            "главная мысль",
            "основная идея",
            "основная мысль",
            "важная мысль",
            "важный момент",
            "важно",
            "важное замечание",
            "самое главное",
            "главное",
            "суть в том",
            "суть заключается",
            "итог",
            "в итоге",
            "подводя итог",
            "резюмируя",
            "в результате",
            "обобщая",
            "вывод",
            "можно сделать вывод",
            "это означает",
            "это значит",
            "следовательно",
            "таким образом",
            "подытожим",
            "короче говоря",
            "если кратко",
            "в целом",
            "в общем",
            "основной вывод",
            "на самом деле важно",
            "ключевой момент"
    };


    /**
     * Извлекаем предложения, содержащие маркеры ключевых идей.
     */
    public List<String> extract(List<String> sentences) {
        List<String> ideas = new ArrayList<>();

        for (String s : sentences) {
            String lower = s.toLowerCase();
            for (String m : MARKERS) {
                if (lower.contains(m)) {
                    ideas.add(s);
                    break;
                }
            }
        }

        return ideas;
    }
}
