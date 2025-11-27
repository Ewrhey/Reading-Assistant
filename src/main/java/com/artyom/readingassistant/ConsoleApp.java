package com.artyom.readingassistant;

import com.artyom.readingassistant.model.ArticleResult;
import com.artyom.readingassistant.service.FormatForPdfService;
import com.artyom.readingassistant.service.PdfExporter;
import com.artyom.readingassistant.service.ReadingPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = "com.artyom.readingassistant")
public class ConsoleApp {

    private static final Logger log = LoggerFactory.getLogger(ConsoleApp.class);

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -jar reading-assistant.jar <article-url>");
            System.exit(1);
        }

        String url = args[0].trim();
        ApplicationContext ctx = SpringApplication.run(ConsoleApp.class, args);

        ReadingPipeline pipeline = ctx.getBean(ReadingPipeline.class);
        FormatForPdfService formatter = ctx.getBean(FormatForPdfService.class);
        PdfExporter pdfExporter = ctx.getBean(PdfExporter.class);

        log.info("Analyzing URL: {}", url);
        ArticleResult result = pipeline.analyze(url);

        String textForPdf = formatter.formatPlainText(result);

        // ensure output directory
        Path outDir = Path.of("output");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);

        String safeName = pdfExporter.sanitizeFileName(result.getTitle() == null || result.getTitle().isBlank()
                ? url.replaceAll("[^a-zA-Z0-9_-]", "_")
                : result.getTitle());
        Path outFile = outDir.resolve(safeName + ".pdf");

        pdfExporter.exportTextToPdf(textForPdf, outFile.toAbsolutePath().toString());

        log.info("Saved PDF to {}", outFile.toAbsolutePath());
        System.out.println("Saved PDF to " + outFile.toAbsolutePath());
        SpringApplication.exit(ctx, () -> 0);
    }
}
