package com.artyom.readingassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReadingAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadingAssistantApplication.class, args);
	}

}
