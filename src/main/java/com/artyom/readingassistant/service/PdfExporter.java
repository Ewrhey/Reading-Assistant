package com.artyom.readingassistant.service;

import com.artyom.readingassistant.model.ArticleResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfExporter {

    /**
     * Главный метод: принимает ArticleResult, формирует итоговый текст,
     * генерирует имя файла и создаёт PDF.
     */
    public void export(ArticleResult result) throws IOException {

        String safeName = sanitizeFileName(result.getTitle());
        String outPath = "C:\\Users\\Artem\\Desktop\\programs\\java\\reading-assistant\\output\\" + safeName + ".pdf";

        StringBuilder sb = new StringBuilder();
        sb.append("TITLE:\n")
                .append(result.getTitle()).append("\n\n")

                .append("SUMMARY:\n")
                .append(result.getSummary()).append("\n\n")

                .append("KEY IDEAS:\n")
                .append(String.join(", ", result.getKeyIdeas())).append("\n\n")

                .append("ACTION ITEMS:\n");
        for (var a : result.getActionItems()) {
            sb.append("- ").append(a.toString()).append("\n");
        }

        exportTextToPdf(sb.toString(), outPath);
    }

    /**
     * Экспорт plain text в PDF.
     */
    public void exportTextToPdf(String text, String outFilePath) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDType0Font font = loadFontOrDefault(doc);

            float fontSize = 11;
            float leading = 1.2f * fontSize;
            float margin = 50;

            PDRectangle box = page.getMediaBox();
            float width = box.getWidth() - 2 * margin;
            float startX = box.getLowerLeftX() + margin;
            float startY = box.getUpperRightY() - margin;

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(startX, startY);

                String[] paragraphs = text.split("\\r?\\n");

                for (String para : paragraphs) {
                    for (String line : wrapText(para, font, fontSize, width)) {
                        content.showText(line);
                        content.newLineAtOffset(0, -leading);
                    }
                    content.newLineAtOffset(0, -leading);
                }

                content.endText();
            }

            File out = new File(outFilePath);
            doc.save(out);
        }
    }

    /**
     * Перенос строк под ограничение ширины.
     */
    private java.util.List<String> wrapText(String text, PDType0Font font, float fontSize, float width) throws IOException {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            String candidate = line.length() == 0 ? w : line + " " + w;
            float size = font.getStringWidth(candidate) / 1000 * fontSize;

            if (size > width) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(w);
                } else {
                    lines.add(candidate);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }

    /**
     * Загрузка шрифта DejaVuSans, если есть.
     */
    private PDType0Font loadFontOrDefault(PDDocument doc) throws IOException {
        InputStream is = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");

        if (is != null) {
            try {
                return PDType0Font.load(doc, is, true);
            } finally {
                is.close();
            }
        }

        return PDType0Font.load(doc,
                PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"),
                true);
    }

    /**
     * Убираем недопустимые символы.
     */
    public String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "article";
        String s = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        s = s.replaceAll("\\s+", "_");
        return s.length() > 120 ? s.substring(0, 120) : s;
    }
}

