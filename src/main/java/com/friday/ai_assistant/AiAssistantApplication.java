package com.friday.ai_assistant;

import org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		exclude = { VertexAiGeminiAutoConfiguration.class }
)
public class AiAssistantApplication {
	public static void main(String[] args) {
		System.setProperty("spring.main.banner-mode", "off");
		SpringApplication.run(AiAssistantApplication.class, args);
	}
}
