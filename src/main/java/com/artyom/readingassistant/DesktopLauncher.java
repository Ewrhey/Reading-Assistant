package com.artyom.readingassistant;

import com.artyom.readingassistant.service.PdfExporter;
import com.artyom.readingassistant.service.ReadingPipeline;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;

public class DesktopLauncher {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(ConsoleApp.class);

        ReadingPipeline pipeline = ctx.getBean(ReadingPipeline.class);
        PdfExporter exporter = ctx.getBean(PdfExporter.class);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Input article URL: ");
        String url = scanner.nextLine();

        try {
            var result = pipeline.analyze(url);
            exporter.export(result);
            System.out.println("PDF created and export in \\output directory.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
