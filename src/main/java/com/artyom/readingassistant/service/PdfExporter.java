package com.artyom.readingassistant.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class PdfExporter {

    /**
     * Exports plain text into a simple PDF.
     * Not fancy but robust for portfolio.
     */
    public void exportTextToPdf(String text, String outFilePath) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            // Load a built-in font that supports Cyrillic — use system fallback if available
            // Try to load font from resources: if you include e.g. "fonts/DejaVuSans.ttf" in resources
            PDType0Font font = loadFontOrDefault(doc);

            float fontSize = 11;
            float leading = 1.2f * fontSize;
            PDRectangle mediaBox = page.getMediaBox();
            float margin = 50;
            float width = mediaBox.getWidth() - 2 * margin;
            float startX = mediaBox.getLowerLeftX() + margin;
            float startY = mediaBox.getUpperRightY() - margin;

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(startX, startY);

                String[] paragraphs = text.split("\\r?\\n");
                for (String para : paragraphs) {
                    // wrap the paragraph manually
                    for (String line : wrapText(para, font, fontSize, width)) {
                        content.showText(line);
                        content.newLineAtOffset(0, -leading);
                    }
                    content.newLineAtOffset(0, -leading); // add extra line between paragraphs
                }

                content.endText();
            }

            // Save
            File out = new File(outFilePath);
            doc.save(out);
        }
    }

    // helper: word-wrap a paragraph for the given width
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
                    // single long word
                    lines.add(candidate);
                    line = new StringBuilder();
                }
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private PDType0Font loadFontOrDefault(PDDocument doc) throws IOException {
        // try to load DejaVuSans from resources/fonts/DejaVuSans.ttf
        InputStream is = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");
        if (is != null) {
            // copy to temp file and load from stream
            try {
                return PDType0Font.load(doc, is, true);
            } finally {
                is.close();
            }
        }
        // fallback: use built-in Helvetica (may not have Cyrillic — still usable)
        return PDType0Font.load(doc, PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"), true);
    }

    // sanitize filename (basic)
    public String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "article";
        String s = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        s = s.replaceAll("\\s+", "_");
        s = s.length() > 120 ? s.substring(0, 120) : s;
        return s;
    }
}
